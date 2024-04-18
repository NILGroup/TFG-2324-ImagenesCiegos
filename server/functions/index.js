const {onRequest} = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");
const { onCall } = require("firebase-functions/v1/https");

const fs = require('fs');

async function descriptor(data) {
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



async function tagging(data) {
	const response = await fetch(
		"https://api-inference.huggingface.co/models/facebook/detr-resnet-50",
		{
			headers: { Authorization: "Bearer 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" },
			method: "POST",
			body: data,
		}
	);
	const result = await response.json();
	return result;
}

async function query(data) {
	const response = await fetch(
		"https://api-inference.huggingface.co/models/Helsinki-NLP/opus-mt-en-es",
		{
			headers: { Authorization: "Bearer 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" },
			method: "POST",
			body: JSON.stringify(data),
		}
	);
	const result = await response.json();
	return result;
}

async function getGenero(data) {
	const response = await fetch(
		"https://api-inference.huggingface.co/models/dima806/man_woman_face_image_detection",
		{
			headers: { Authorization: "Bearer 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" },
			method: "POST",
			body: data,
		}
	);
	const result = await response.json();
	return result;
}
exports.genero = onCall((data, response) => {
	console.log("Función de genero conectada");
	const base64 = data.url;
	const buffer  = Buffer.from(base64,'base64');

	return getGenero(buffer).then((response) => {
		return JSON.stringify(response);
	});
});

exports.descripImagen = onCall((data, response) => {
	console.log("Función de analisis conectada");
	const base64 = data.url;
	const buffer  = Buffer.from(base64,'base64');

	return descriptor(buffer).then((response) => {
		return JSON.stringify(response);
	});
 });

 exports.tagsImagen = onCall((data, response) => {
	console.log("Función de tags conectada");
	const base64 = data.url;
	const buffer  = Buffer.from(base64,'base64');
	return tagging(buffer).then((response) => {
		return JSON.stringify(response);
	});
 });

 exports.traducDescrip = onCall((data, response) => {
	console.log("Función de traduccion conectada");
	return query({"inputs": data.texto}).then((response) => {
		return JSON.stringify(response);
	});
 });
