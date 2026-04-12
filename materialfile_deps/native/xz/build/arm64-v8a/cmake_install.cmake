# Install script for directory: D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/src

# Set the install prefix
if(NOT DEFINED CMAKE_INSTALL_PREFIX)
  set(CMAKE_INSTALL_PREFIX "D:/AndroidApp/MaterialFile/prebuild/native/xz")
endif()
string(REGEX REPLACE "/$" "" CMAKE_INSTALL_PREFIX "${CMAKE_INSTALL_PREFIX}")

# Set the install configuration name.
if(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)
  if(BUILD_TYPE)
    string(REGEX REPLACE "^[^A-Za-z0-9_]+" ""
           CMAKE_INSTALL_CONFIG_NAME "${BUILD_TYPE}")
  else()
    set(CMAKE_INSTALL_CONFIG_NAME "Release")
  endif()
  message(STATUS "Install configuration: \"${CMAKE_INSTALL_CONFIG_NAME}\"")
endif()

# Set the component getting installed.
if(NOT CMAKE_INSTALL_COMPONENT)
  if(COMPONENT)
    message(STATUS "Install component: \"${COMPONENT}\"")
    set(CMAKE_INSTALL_COMPONENT "${COMPONENT}")
  else()
    set(CMAKE_INSTALL_COMPONENT)
  endif()
endif()

# Install shared libraries without execute permission?
if(NOT DEFINED CMAKE_INSTALL_SO_NO_EXE)
  set(CMAKE_INSTALL_SO_NO_EXE "0")
endif()

# Is this installation the result of a crosscompile?
if(NOT DEFINED CMAKE_CROSSCOMPILING)
  set(CMAKE_CROSSCOMPILING "TRUE")
endif()

# Set default install directory permissions.
if(NOT DEFINED CMAKE_OBJDUMP)
  set(CMAKE_OBJDUMP "D:/AndroidApp/MaterialFile/tools/android-sdk/ndk/28.1.13356709/toolchains/llvm/prebuilt/windows-x86_64/bin/llvm-objdump.exe")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xliblzma_Developmentx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/lib" TYPE STATIC_LIBRARY FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/build/arm64-v8a/liblzma.a")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xliblzma_Developmentx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/include" TYPE DIRECTORY FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/src/src/liblzma/api/" FILES_MATCHING REGEX "/[^/]*\\.h$")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xliblzma_Developmentx" OR NOT CMAKE_INSTALL_COMPONENT)
  if(EXISTS "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/lib/cmake/liblzma/liblzma-targets.cmake")
    file(DIFFERENT EXPORT_FILE_CHANGED FILES
         "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/lib/cmake/liblzma/liblzma-targets.cmake"
         "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/build/arm64-v8a/CMakeFiles/Export/lib/cmake/liblzma/liblzma-targets.cmake")
    if(EXPORT_FILE_CHANGED)
      file(GLOB OLD_CONFIG_FILES "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/lib/cmake/liblzma/liblzma-targets-*.cmake")
      if(OLD_CONFIG_FILES)
        message(STATUS "Old export file \"$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/lib/cmake/liblzma/liblzma-targets.cmake\" will be replaced.  Removing files [${OLD_CONFIG_FILES}].")
        file(REMOVE ${OLD_CONFIG_FILES})
      endif()
    endif()
  endif()
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/lib/cmake/liblzma" TYPE FILE FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/build/arm64-v8a/CMakeFiles/Export/lib/cmake/liblzma/liblzma-targets.cmake")
  if("${CMAKE_INSTALL_CONFIG_NAME}" MATCHES "^([Rr][Ee][Ll][Ee][Aa][Ss][Ee])$")
    file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/lib/cmake/liblzma" TYPE FILE FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/build/arm64-v8a/CMakeFiles/Export/lib/cmake/liblzma/liblzma-targets-release.cmake")
  endif()
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xliblzma_Developmentx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/lib/cmake/liblzma" TYPE FILE FILES
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/build/arm64-v8a/liblzma-config.cmake"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/build/arm64-v8a/liblzma-config-version.cmake"
    )
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xliblzma_Developmentx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/lib/pkgconfig" TYPE FILE FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/build/arm64-v8a/liblzma.pc")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xxzdec_Runtimex" OR NOT CMAKE_INSTALL_COMPONENT)
  if(EXISTS "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/xzdec" AND
     NOT IS_SYMLINK "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/xzdec")
    file(RPATH_CHECK
         FILE "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/xzdec"
         RPATH "")
  endif()
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/bin" TYPE EXECUTABLE FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/build/arm64-v8a/xzdec")
  if(EXISTS "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/xzdec" AND
     NOT IS_SYMLINK "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/xzdec")
    if(CMAKE_INSTALL_DO_STRIP)
      execute_process(COMMAND "D:/AndroidApp/MaterialFile/tools/android-sdk/ndk/28.1.13356709/toolchains/llvm/prebuilt/windows-x86_64/bin/llvm-strip.exe" "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/xzdec")
    endif()
  endif()
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xlzmadec_Runtimex" OR NOT CMAKE_INSTALL_COMPONENT)
  if(EXISTS "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/lzmadec" AND
     NOT IS_SYMLINK "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/lzmadec")
    file(RPATH_CHECK
         FILE "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/lzmadec"
         RPATH "")
  endif()
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/bin" TYPE EXECUTABLE FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/build/arm64-v8a/lzmadec")
  if(EXISTS "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/lzmadec" AND
     NOT IS_SYMLINK "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/lzmadec")
    if(CMAKE_INSTALL_DO_STRIP)
      execute_process(COMMAND "D:/AndroidApp/MaterialFile/tools/android-sdk/ndk/28.1.13356709/toolchains/llvm/prebuilt/windows-x86_64/bin/llvm-strip.exe" "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/lzmadec")
    endif()
  endif()
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xxzdec_Documentationx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/man/man1" TYPE FILE FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/src/src/xzdec/xzdec.1")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xxzdec_Documentationx" OR NOT CMAKE_INSTALL_COMPONENT)
  set(D "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/share/man//man1")
                 foreach(L lzmadec)
                     file(CREATE_LINK "xzdec.1"
                                      "${D}/${L}.1"
                                      SYMBOLIC)
                 endforeach()
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xlzmainfo_Runtimex" OR NOT CMAKE_INSTALL_COMPONENT)
  if(EXISTS "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/lzmainfo" AND
     NOT IS_SYMLINK "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/lzmainfo")
    file(RPATH_CHECK
         FILE "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/lzmainfo"
         RPATH "")
  endif()
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/bin" TYPE EXECUTABLE FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/build/arm64-v8a/lzmainfo")
  if(EXISTS "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/lzmainfo" AND
     NOT IS_SYMLINK "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/lzmainfo")
    if(CMAKE_INSTALL_DO_STRIP)
      execute_process(COMMAND "D:/AndroidApp/MaterialFile/tools/android-sdk/ndk/28.1.13356709/toolchains/llvm/prebuilt/windows-x86_64/bin/llvm-strip.exe" "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/lzmainfo")
    endif()
  endif()
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xlzmainfo_Documentationx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/man/man1" TYPE FILE FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/src/src/lzmainfo/lzmainfo.1")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xlzmainfo_Documentationx" OR NOT CMAKE_INSTALL_COMPONENT)
  set(D "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/share/man//man1")
                 foreach(L )
                     file(CREATE_LINK "lzmainfo.1"
                                      "${D}/${L}.1"
                                      SYMBOLIC)
                 endforeach()
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xxz_Runtimex" OR NOT CMAKE_INSTALL_COMPONENT)
  if(EXISTS "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/xz" AND
     NOT IS_SYMLINK "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/xz")
    file(RPATH_CHECK
         FILE "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/xz"
         RPATH "")
  endif()
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/bin" TYPE EXECUTABLE FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/build/arm64-v8a/xz")
  if(EXISTS "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/xz" AND
     NOT IS_SYMLINK "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/xz")
    if(CMAKE_INSTALL_DO_STRIP)
      execute_process(COMMAND "D:/AndroidApp/MaterialFile/tools/android-sdk/ndk/28.1.13356709/toolchains/llvm/prebuilt/windows-x86_64/bin/llvm-strip.exe" "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin/xz")
    endif()
  endif()
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xxz_Runtimex" OR NOT CMAKE_INSTALL_COMPONENT)
  set(D "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin")
                 foreach(L )
                     file(CREATE_LINK "xz"
                                      "${D}/${L}"
                                      SYMBOLIC)
                 endforeach()
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xxz_Documentationx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/man/man1" TYPE FILE FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/src/src/xz/xz.1")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xxz_Documentationx" OR NOT CMAKE_INSTALL_COMPONENT)
  set(D "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/share/man//man1")
                 foreach(L )
                     file(CREATE_LINK "xz.1"
                                      "${D}/${L}.1"
                                      SYMBOLIC)
                 endforeach()
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xscripts_Runtimex" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/bin" TYPE PROGRAM FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/build/arm64-v8a/xzdiff")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xscripts_Runtimex" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/bin" TYPE PROGRAM FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/build/arm64-v8a/xzgrep")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xscripts_Runtimex" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/bin" TYPE PROGRAM FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/build/arm64-v8a/xzmore")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xscripts_Runtimex" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/bin" TYPE PROGRAM FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/build/arm64-v8a/xzless")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xscripts_Runtimex" OR NOT CMAKE_INSTALL_COMPONENT)
  set(D "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin")
                 foreach(L xzcmp)
                     file(CREATE_LINK "xzdiff"
                                      "${D}/${L}"
                                      SYMBOLIC)
                 endforeach()
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xscripts_Runtimex" OR NOT CMAKE_INSTALL_COMPONENT)
  set(D "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin")
                 foreach(L xzegrep;xzfgrep)
                     file(CREATE_LINK "xzgrep"
                                      "${D}/${L}"
                                      SYMBOLIC)
                 endforeach()
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xscripts_Runtimex" OR NOT CMAKE_INSTALL_COMPONENT)
  set(D "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin")
                 foreach(L )
                     file(CREATE_LINK "xzmore"
                                      "${D}/${L}"
                                      SYMBOLIC)
                 endforeach()
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xscripts_Runtimex" OR NOT CMAKE_INSTALL_COMPONENT)
  set(D "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/bin")
                 foreach(L )
                     file(CREATE_LINK "xzless"
                                      "${D}/${L}"
                                      SYMBOLIC)
                 endforeach()
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xscripts_Documentationx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/man/man1" TYPE FILE FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/src/src/scripts/xzdiff.1")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xscripts_Documentationx" OR NOT CMAKE_INSTALL_COMPONENT)
  set(D "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/share/man//man1")
                 foreach(L xzcmp)
                     file(CREATE_LINK "xzdiff.1"
                                      "${D}/${L}.1"
                                      SYMBOLIC)
                 endforeach()
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xscripts_Documentationx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/man/man1" TYPE FILE FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/src/src/scripts/xzgrep.1")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xscripts_Documentationx" OR NOT CMAKE_INSTALL_COMPONENT)
  set(D "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/share/man//man1")
                 foreach(L xzegrep;xzfgrep)
                     file(CREATE_LINK "xzgrep.1"
                                      "${D}/${L}.1"
                                      SYMBOLIC)
                 endforeach()
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xscripts_Documentationx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/man/man1" TYPE FILE FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/src/src/scripts/xzmore.1")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xscripts_Documentationx" OR NOT CMAKE_INSTALL_COMPONENT)
  set(D "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/share/man//man1")
                 foreach(L )
                     file(CREATE_LINK "xzmore.1"
                                      "${D}/${L}.1"
                                      SYMBOLIC)
                 endforeach()
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xscripts_Documentationx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/man/man1" TYPE FILE FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/src/src/scripts/xzless.1")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xscripts_Documentationx" OR NOT CMAKE_INSTALL_COMPONENT)
  set(D "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/share/man//man1")
                 foreach(L )
                     file(CREATE_LINK "xzless.1"
                                      "${D}/${L}.1"
                                      SYMBOLIC)
                 endforeach()
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xliblzma_Documentationx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/doc/xz" TYPE DIRECTORY FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/src/doc/examples")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xDocumentationx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/doc/xz" TYPE FILE FILES
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/src/AUTHORS"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/src/COPYING"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/src/COPYING.0BSD"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/src/COPYING.GPLv2"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/src/NEWS"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/src/README"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/src/THANKS"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/src/doc/faq.txt"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/src/doc/history.txt"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/src/doc/lzma-file-format.txt"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/src/doc/xz-file-format.txt"
    )
endif()

if(CMAKE_INSTALL_COMPONENT)
  set(CMAKE_INSTALL_MANIFEST "install_manifest_${CMAKE_INSTALL_COMPONENT}.txt")
else()
  set(CMAKE_INSTALL_MANIFEST "install_manifest.txt")
endif()

string(REPLACE ";" "\n" CMAKE_INSTALL_MANIFEST_CONTENT
       "${CMAKE_INSTALL_MANIFEST_FILES}")
file(WRITE "D:/AndroidApp/MaterialFile/materialfile_deps/native/xz/build/arm64-v8a/${CMAKE_INSTALL_MANIFEST}"
     "${CMAKE_INSTALL_MANIFEST_CONTENT}")
