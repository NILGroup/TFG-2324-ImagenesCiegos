/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {onRequest} = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");
const { onCall } = require("firebase-functions/v1/https");

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

exports.helloWorld = onRequest((request, response) => {
   console.log("App conectada");
   response.send("Hello from Firebase!");
});

exports.prueba = onCall((data, response) => {
    console.log("Funcion de prueba CONECTADA");
    return "salida de prueba";
 });
