import math
import os
from dotenv import load_dotenv
import sys

from pymongo import MongoClient, WriteConcern

from query import *

load_dotenv()
HOST =  os.getenv('HOST')

CORE_DB = 'selcMsCore'
INSTITUTION_COLLECTION = 'Institution'

USERS_DB = 'selcUser'
USER_INSTITUTION_COLLECTION = 'userInstitutions'

BATCH_SIZE = 100
START_PAGE = 0
INSTITUTION_ID = sys.argv[1]

def migrate_institution_description(client):
    institutions_size = client[CORE_DB][INSTITUTION_COLLECTION].count_documents({})
    print(INSTITUTION_COLLECTION + " size: " + str(institutions_size))
    pages = math.ceil(institutions_size / BATCH_SIZE)

    for page in range(START_PAGE, pages):
        print("Start page " + str(page) + "/" + str(pages))

        if INSTITUTION_ID is None:
            institution_pages = client[CORE_DB][INSTITUTION_COLLECTION].aggregate(
                        get_institutions(page, BATCH_SIZE)
                    )
        else:
             institution_pages = client[CORE_DB][INSTITUTION_COLLECTION].aggregate(
                        get_institution(INSTITUTION_ID)
                    )

        for institution in institution_pages:
            institution_desc = institution.get('description')
            institution_id = institution.get('_id')

            client[USERS_DB][USER_INSTITUTION_COLLECTION].update_many(
                {"institutionId": institution_id},
                {"$set": {"institutionDescription": institution_desc}},
                False)

        print("End page " + str(page) + "/" + str(pages))

if __name__ == "__main__":
    client = MongoClient(HOST)
    with client.start_session() as session:
        session.start_transaction()
        migrate_institution_description(client)
        session.commit_transaction()

    client.close()