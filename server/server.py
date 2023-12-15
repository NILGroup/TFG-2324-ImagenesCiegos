import flask
import requests

app = flask.Flask(__name__)

#to display the connection status
@app.route('/', methods=['GET'])
def handle_call():
    return "Successfully Connected"

#the get method. when we call this, it just return the text "Hey!! I'm the fact you got!!!"
@app.route('/getfact', methods=['GET'])
def get_fact():
    return "Hey!! I'm the fact you got!!!"

def upload_file():
    if request.method == 'POST':
        if 'file' not in request.files:
            return 'No file part'
        file = request.files['file']
        if file.filename == '':
            return 'No selected file'
        if file:
            file.save('uploads/' + file.filename)  # Guarda el archivo en la carpeta 'uploads' del servidor
            return 'Archivo subido con éxito'
    return render_template('upload.html')

#the post method. when we call this with a string containing a name, it will return the name with the text "I got your name"
@app.route('/getname/<name>', methods=['POST'])
def extract_name(name):
    return "I got your name "+name;

#this commands the script to run in the given port
if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000, debug=True)