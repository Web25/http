package org.web25.http

class ElementNotFoundException(name: String, fieldName: String) : RuntimeException("No value with name $name found in $fieldName")