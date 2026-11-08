const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Feedback: Anyone can create valid feedback with required fields", async () => {
  const context = testEnv.unauthenticatedContext();
  const db = context.firestore();

  const validPayload = {
    category: "BUG",
    message: "Aplikacja zawiesza się na rozdziale 3",
    contactEmail: "user@example.com",
    createdAt: new Date(),
    deviceInfo: {
      osVersion: "Android 14 (API 34)",
      sdkInt: 34,
      manufacturer: "Google",
      model: "Pixel 8",
      device: "shiba",
      appVersion: "5.0",
      appVersionCode: 5,
      networkType: "Wi-Fi",
      screenDensity: "420dpi",
      preferredBibleSource: "pismoswiete"
    }
  };

  await assertSucceeds(db.collection("feedback").doc("valid_feedback_1").set(validPayload));
});

test("Feedback: Rejects invalid feedback (short message or missing required fields)", async () => {
  const context = testEnv.unauthenticatedContext();
  const db = context.firestore();

  // Invalid: message too short
  await assertFails(db.collection("feedback").doc("invalid_1").set({
    category: "BUG",
    message: "hi",
    createdAt: new Date(),
    deviceInfo: { osVersion: "14", model: "Pixel" }
  }));

  // Invalid: invalid category enum
  await assertFails(db.collection("feedback").doc("invalid_2").set({
    category: "UNKNOWN_CATEGORY",
    message: "Poprawne powiadomienie o błędzie",
    createdAt: new Date(),
    deviceInfo: { osVersion: "14", model: "Pixel" }
  }));

  // Invalid: ghost field
  await assertFails(db.collection("feedback").doc("invalid_3").set({
    category: "BUG",
    message: "Poprawne powiadomienie o błędzie",
    ghostField: "hacker",
    createdAt: new Date(),
    deviceInfo: { osVersion: "14", model: "Pixel" }
  }));
});

test("Feedback: Reads, updates, and deletes are strictly forbidden from client", async () => {
  // Seed a feedback document using rules-disabled context
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("feedback").doc("test_doc").set({
      category: "SUGGESTION",
      message: "Propozycja dodania zakładki",
      createdAt: new Date(),
      deviceInfo: { osVersion: "14", model: "Pixel" }
    });
  });

  const context = testEnv.unauthenticatedContext();
  const db = context.firestore();

  // Client cannot get
  await assertFails(db.collection("feedback").doc("test_doc").get());

  // Client cannot list
  await assertFails(db.collection("feedback").get());

  // Client cannot update
  await assertFails(db.collection("feedback").doc("test_doc").update({
    message: "Hacked message"
  }));

  // Client cannot delete
  await assertFails(db.collection("feedback").doc("test_doc").delete());
});
