/*
 * Copyright (c) 2026 advancefilemanager
 * All Rights Reserved.
 */

package com.advancefilemanager.provider.common

import java8.nio.file.FileSystemException

class FileStoreNotFoundException(file: String?) : FileSystemException(file)
