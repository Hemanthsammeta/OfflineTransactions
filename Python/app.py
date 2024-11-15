from flask import Flask, request, jsonify
import pickle
import numpy as np

# Load the pre-trained model
with open('fraud_detection_model.pkl', 'rb') as file:
    model = pickle.load(file)

app = Flask(__name__)

@app.route('/detect_fraud', methods=['POST'])
def detect_fraud():
    try:
        data = request.json
        amount = data.get('amount')
        
        if amount is None:
            return jsonify({'error': 'Amount is required'}), 400
        
        # Predict fraud based on transaction amount
        prediction = model.predict(np.array([[amount]]))
        is_fraud = prediction[0] == 1

        return jsonify({'is_fraud': is_fraud})
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True, port=5000)
