from bson.int64 import Int64


# User -> bindings.InstitutionName
# UserInstitution -> InstitutionDescription
# UserInfo -> products.InstitutionName

def paging(page, size):
    return {
        '$sort': {
            '_id': 1
        }
    }, {
        '$skip': Int64(page * size)
    }, {
        '$limit': Int64(size)
    }


def get_institutions(page, size):
    return [
        {
            '$match': {
                'description': {
                    '$exists': True
                }
            }
        },
        *paging(page, size)
    ]


def get_delegations(page, size):
    return [
        {
            '$match': {
                '$or': [
                    {
                        'toTaxCode': {
                            '$exists': False
                        }
                    }, {
                        'fromTaxCode': {
                            '$exists': False
                        }
                    },
                    {
                        'toType': {
                            '$exists': False
                        }
                    }, {
                        'fromType': {
                            '$exists': False
                        }
                    }
                ]
            }

        },
        *paging(page, size)
    ]


def get_institutions_from_onboardings(page, size):
    return [
            {
                '$match': {
                    'status': 'COMPLETED',
                    'institution.description': {
                        '$exists': True
                    }
                }
            },
            *paging(page, size)
    ]


def get_institutions_whithout_description(page, size):
    return [
            {
                '$match': {
                    'onboarding': {
                        '$exists': True
                    },
                    'description': {
                        '$exists': False
                    }
                }
            },
            *paging(page, size)
    ]


def count_institutions_whithout_description():
    return [
        {
            '$match': {
                'onboarding': {
                    '$exists': True
                },
                'description': {
                    '$exists': False
                }
            }
        },
        {
            '$count': 'count'
        }
    ]


def count_institutions_with_active_onboarding():
    return [
        {
            "$match": {
                "onboarding.status": "ACTIVE"
            }
        },
        {
            '$count': 'count'
        }
    ]


def get_institutions_with_active_onboarding(page, size):
    return [
        {
            "$match": {
                "onboarding.status": "ACTIVE"
            }
        },
        *paging(page, size)
    ]


def count_delegations():
    return [
        {
            '$match': {
                'toTaxCode': {
                    '$exists': False
                },
                'fromTaxCode': {
                    '$exists': False
                }
            }
        },
        {
            '$count': 'count'
        }
    ]


def count_institutions_from_onboardings():
    return [
        {
            '$match': {
                'status': 'COMPLETED'
            }
        },
        {
            '$count': 'count'
        }
    ]

def get_institution(institutionId):
    return {
               '_id': institutionId
           }

def get_onboarding(institutionId):
    return {
        "institution.id": institutionId,
        "status": "COMPLETED"
    }

def user_institution_from_user_query(page, size):
    return [
        *paging(page, size),
        {
            '$unwind': {
                'path': '$bindings'
            }
        }, {
            '$lookup': {
                'from': 'Institution',
                'localField': 'bindings.institutionId',
                'foreignField': '_id',
                'as': 'institution'
            }
        }, {
            '$set': {
                'institution': {
                    '$arrayElemAt': [
                        '$institution', 0
                    ]
                }
            }
        }, {
            '$project': {
                '_id': 0,
                'userId': '$_id',
                'institutionId': '$bindings.institutionId',
                'institutionDescription': '$institution.description',
                'institutionRootName': '$institution.parentDescription',
                'products': {
                    '$map': {
                        'input': '$bindings.products',
                        'as': 'product',
                        'in': {
                            'productId': '$$product.productId',
                            'relationshipId': '$$product.relationshipId',
                            'tokenId': '$$product.tokenId',
                            'status': '$$product.status',
                            'productRole': '$$product.productRole',
                            'role': '$$product.role',
                            'env': '$$product.env',
                            'createdAt': {
                                '$toDate': '$$product.createdAt'
                            },
                            'updatedAt': {
                                '$toDate': '$$product.updatedAt'
                            }
                        }
                    }
                }
            }
        }
    ]


def user_info_from_user_institution_query(db, collection, userId):
    pipeline = []

    if userId is not None:
        pipeline.append({
            '$match': {
                'userId': userId
            }
        })

    pipeline.extend([
        {
            '$group': {
                '_id': '$userId',
                'institutions': {
                    '$addToSet': {
                        '$let': {
                            'vars': {
                                'status': {
                                    '$let': {
                                        'vars': {
                                            'tmp': {
                                                '$setIntersection': '$products.status'
                                            }
                                        },
                                        'in': {
                                            '$cond': [
                                                {
                                                    '$in': [
                                                        'ACTIVE', '$$tmp'
                                                    ]
                                                }, 'ACTIVE', {
                                                    '$cond': [
                                                        {
                                                            '$in': [
                                                                'PENDING', '$$tmp'
                                                            ]
                                                        }, 'PENDING', {
                                                            '$cond': [
                                                                {
                                                                    '$in': [
                                                                        'TOBEVALIDATED', '$$tmp'
                                                                    ]
                                                                }, 'TOBEVALIDATED', None
                                                            ]
                                                        }
                                                    ]
                                                }
                                            ]
                                        }
                                    }
                                }
                            },
                            'in': {
                                'institutionId': '$institutionId',
                                'institutionName': '$institutionDescription',
                                'institutionRootName': '$institutionRootName',
                                'status': '$$status',
                                'role': {
                                    '$let': {
                                        'vars': {
                                            'roles': {
                                                '$setIntersection': '$products.role'
                                            }
                                        },
                                        'in': {
                                            '$cond': [
                                                {
                                                    '$in': [
                                                        'MANAGER', '$$roles'
                                                    ]
                                                }, 'MANAGER', {
                                                    '$cond': [
                                                        {
                                                            '$in': [
                                                                'DELEGATE', '$$roles'
                                                            ]
                                                        }, 'DELEGATE', {
                                                            '$cond': [
                                                                {
                                                                    '$in': [
                                                                        'SUB_DELEGATE', '$$roles'
                                                                    ]
                                                                }, 'SUB_DELEGATE', 'OPERATOR'
                                                            ]
                                                        }
                                                    ]
                                                }
                                            ]
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ])

    return pipeline
