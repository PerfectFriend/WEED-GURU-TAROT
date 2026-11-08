const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

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

test("Unauthenticated user: cannot read user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: can read and write own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "Alice",
      weed: 100
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: cannot read another user's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      username: "Bob",
      weed: 50
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});

test("Authenticated user: can manage own readings", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("readings").doc("reading_1").set({
      id: "reading_1",
      userId: ALICE_UID,
      question: "Will the cyber matrix hold?",
      report: "Stable connection",
      timestamp: Date.now()
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("readings").doc("reading_1").get()
  );
});

test("Market listings: authenticated can read and create own listings", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("market").doc("listing_1").set({
      id: "listing_1",
      sellerId: ALICE_UID,
      cardIndex: 0,
      price: 150,
      timestamp: Date.now()
    })
  );
  await assertSucceeds(aliceDb.collection("market").doc("listing_1").get());
});

test("Market listings: user cannot update another user's listing", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("market").doc("listing_2").set({
      id: "listing_2",
      sellerId: BOB_UID,
      cardIndex: 1,
      price: 200,
      timestamp: Date.now()
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("market").doc("listing_2").update({ price: 100 })
  );
});
