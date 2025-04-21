from pymongo import MongoClient

# MongoDB 연결 설정
client = MongoClient("mongodb+srv://iotai:I2XEF5g5MeMbIij7@cluster0.tm3m3ep.mongodb.net/sparkdb?retryWrites=true&w=majority")
db = client["sparkdb"]         # 👉 사용할 DB 이름
collection = db["sparkdb"]   # 👉 사용할 컬렉션 이름

# 조건: avg_temp가 없거나 null이거나 "N/A"인 경우
delete_query = {
    "$or": [
        {"avg_temp": {"$exists": False}},     # 필드 자체가 없음
        {"avg_temp": None},                   # null 값
        {"avg_temp": "N/A"}                   # 문자열로 "N/A"
    ]
}

# 삭제 수행
result = collection.delete_many(delete_query)

print(f"삭제된 문서 수: {result.deleted_count}")
