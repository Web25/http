package org.web25.http.server.config

import org.web25.http.server.config.PropertiesConfigurator
import java.io.File
import java.io.FileInputStream

class FileConfigurator(file: File) : PropertiesConfigurator(FileInputStream(file))