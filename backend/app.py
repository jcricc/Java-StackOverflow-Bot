import requests
from flask import Flask, request, jsonify
from bs4 import BeautifulSoup  # Import BeautifulSoup to clean HTML content

app = Flask(__name__)

@app.route('/scrape', methods=['GET'])
def scrape():
    query = request.args.get('query')
    
    if not query:
        return jsonify({"error": "No query provided"}), 400

    response_data = search_stackoverflow_api(query)
    return jsonify(response_data)

def search_stackoverflow_api(query):
    try:
        # Step 1: Search for the relevant question
        search_url = "https://api.stackexchange.com/2.3/search/advanced"
        params = {
            'order': 'desc',
            'sort': 'relevance',
            'q': query,
            'site': 'stackoverflow'
        }
        search_response = requests.get(search_url, params=params)
        search_data = search_response.json()

        # Check if there are results
        if not search_data.get("items"):
            return {"error": "No results found"}

        # Step 2: Get the first question's ID
        first_result = search_data["items"][0]
        question_id = first_result.get("question_id")
        question_title = first_result.get("title", "No title found")
        question_link = first_result.get("link", "No link found")

        # Step 3: Get the answers for the question
        answers_url = f"https://api.stackexchange.com/2.3/questions/{question_id}/answers"
        answer_params = {
            'order': 'desc',
            'sort': 'votes',
            'site': 'stackoverflow',
            'filter': 'withbody'  # This ensures we get the answer body
        }
        answer_response = requests.get(answers_url, params=answer_params)
        answer_data = answer_response.json()

        # Step 4: Get the top answer and clean HTML tags
        if answer_data.get("items"):
            top_answer = answer_data["items"][0].get("body", "No answer found")
            clean_answer = clean_html(top_answer)  # Clean HTML from the answer
        else:
            clean_answer = "No answers found"

        # Return the question title, link, and the cleaned answer
        return {
            "title": question_title,
            "link": question_link,
            "answer": clean_answer
        }

    except Exception as e:
        return {"error": str(e)}

def clean_html(raw_html):
    # Use BeautifulSoup to clean the HTML and extract plain text
    soup = BeautifulSoup(raw_html, "html.parser")
    return soup.get_text()

if __name__ == '__main__':
    app.run(debug=True, port=5000)
