package org.web25.http

class HeaderList: MappedList<String>("header", stringOperator = String::toLowerCase)