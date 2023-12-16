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

const fs = require('fs');

async function query(filename) {
	const data = fs.readFileSync(url);
	const response = await fetch(
		"https://api-inference.huggingface.co/models/Salesforce/blip-image-captioning-large",
		{
			headers: { Authorization: "Bearer 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" },
			method: "POST",
			body: data,
		}
	);
	const result = await response.json();
	return result;
}

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

exports.helloWorld = onRequest((request, response) => {
   console.log("App conectada");
   response.send("Hello from Firebase!");
});

exports.descripImagen = onCall((data, response) => {
   console.log("Funcion de prueba CONECTADA");
   var red = "";
   query(data.url).then((response) => {
      red+= "ret" + JSON.stringify(response);
   });
   return red;
 });
