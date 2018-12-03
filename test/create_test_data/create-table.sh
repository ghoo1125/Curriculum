#!/bin/bash

# create-table
aws dynamodb create-table \
--table-name Course \
--attribute-definitions AttributeName=TeacherId,AttributeType=N AttributeName=CourseName,AttributeType=S \
--key-schema AttributeName=TeacherId,KeyType=HASH AttributeName=CourseName,KeyType=RANGE \
--provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
--endpoint-url http://localhost:8000

# batch-write-item
# DeleteRequest: Perform a DeleteItem operation on specified item.
# PutRequest: Perform a PutItem operation on specified item.
# BatchWriteItem can write up to 16 MB of data, which can comprise as many as 25 put or delete requests. 
aws dynamodb batch-write-item \
--request-items file://request-items.json \
--endpoint-url http://localhost:8000

# update-table
# Update Global Secondary Index for Course table. The GSI can also be created with create-table.
# Local Secondary Index MUST create with create-table.
aws dynamodb update-table \
--table-name Course \
--global-secondary-index-updates file://gsi.json \
--attribute-definitions AttributeName=CourseName,AttributeType=S AttributeName=TeacherName,AttributeType=S \
--endpoint-url http://localhost:8000
:'
# update-item
aws dynamodb update-item \
--table-name Course \
--key file://key.json \
--update-expression "SET #TN = :tn, #S = :s" \
--expression-attribute-names file://update_item_names_expression.json \
--expression-attribute-values file://update_item_values_expression.json \
--return-values ALL_NEW \
--endpoint-url http://localhost:8000

# delete-item
aws dynamodb delete-item \
--table-name Course \
--key file://key.json \
--return-values ALL_OLD \
--endpoint-url http://localhost:8000

# get-item
aws dynamodb get-item \
--table-name Course \
--key file://key.json \
--endpoint-url http://localhost:8000

# scan table
aws dynamodb scan \
--table-name Course \
--endpoint-url http://localhost:8000

# scan index of table
aws dynamodb scan \
--table-name Course \
--index-name ListAllCourses \
--endpoint-url http://localhost:8000

# query
# --projection-expression: A string that identifies one or more attributes to retrieve from the table.
aws dynamodb query \
--table-name Course \
--projection-expression "CourseName" \
--key-condition-expression "TeacherId = :tid AND CourseName <= :cn" \
--expression-attribute-values file://query_by_tid_expression.json \
--endpoint-url http://localhost:8000
'
