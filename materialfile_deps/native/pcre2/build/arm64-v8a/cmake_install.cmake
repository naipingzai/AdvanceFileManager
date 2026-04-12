# Install script for directory: D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src

# Set the install prefix
if(NOT DEFINED CMAKE_INSTALL_PREFIX)
  set(CMAKE_INSTALL_PREFIX "D:/AndroidApp/MaterialFile/prebuild/native/pcre2")
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

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xUnspecifiedx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/lib" TYPE STATIC_LIBRARY FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/build/arm64-v8a/libpcre2-8.a")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xUnspecifiedx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/lib" TYPE STATIC_LIBRARY FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/build/arm64-v8a/libpcre2-posix.a")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xUnspecifiedx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/lib/pkgconfig" TYPE FILE FILES
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/build/arm64-v8a/libpcre2-posix.pc"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/build/arm64-v8a/libpcre2-8.pc"
    )
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xUnspecifiedx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/bin" TYPE FILE PERMISSIONS OWNER_WRITE OWNER_READ OWNER_EXECUTE GROUP_READ GROUP_EXECUTE WORLD_READ WORLD_EXECUTE FILES "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/build/arm64-v8a/pcre2-config")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xUnspecifiedx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/include" TYPE FILE FILES
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/build/arm64-v8a/pcre2.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/src/pcre2posix.h"
    )
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xUnspecifiedx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/cmake" TYPE FILE FILES
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/build/arm64-v8a/cmake/pcre2-config.cmake"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/build/arm64-v8a/cmake/pcre2-config-version.cmake"
    )
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xUnspecifiedx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/man/man1" TYPE FILE FILES
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2-config.1"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2grep.1"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2test.1"
    )
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xUnspecifiedx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/man/man3" TYPE FILE FILES
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_callout_enumerate.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_code_copy.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_code_copy_with_tables.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_code_free.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_compile.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_compile_context_copy.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_compile_context_create.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_compile_context_free.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_config.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_convert_context_copy.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_convert_context_create.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_convert_context_free.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_converted_pattern_free.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_dfa_match.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_general_context_copy.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_general_context_create.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_general_context_free.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_get_error_message.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_get_mark.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_get_match_data_heapframes_size.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_get_match_data_size.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_get_ovector_count.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_get_ovector_pointer.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_get_startchar.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_jit_compile.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_jit_free_unused_memory.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_jit_match.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_jit_stack_assign.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_jit_stack_create.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_jit_stack_free.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_maketables.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_maketables_free.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_match.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_match_context_copy.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_match_context_create.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_match_context_free.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_match_data_create.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_match_data_create_from_pattern.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_match_data_free.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_pattern_convert.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_pattern_info.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_serialize_decode.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_serialize_encode.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_serialize_free.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_serialize_get_number_of_codes.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_set_bsr.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_set_callout.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_set_character_tables.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_set_compile_extra_options.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_set_compile_recursion_guard.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_set_depth_limit.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_set_glob_escape.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_set_glob_separator.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_set_heap_limit.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_set_match_limit.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_set_max_pattern_compiled_length.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_set_max_pattern_length.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_set_max_varlookbehind.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_set_newline.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_set_offset_limit.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_set_parens_nest_limit.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_set_recursion_limit.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_set_recursion_memory_management.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_set_substitute_callout.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_substitute.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_substring_copy_byname.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_substring_copy_bynumber.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_substring_free.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_substring_get_byname.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_substring_get_bynumber.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_substring_length_byname.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_substring_length_bynumber.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_substring_list_free.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_substring_list_get.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_substring_nametable_scan.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2_substring_number_from_name.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2api.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2build.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2callout.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2compat.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2convert.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2demo.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2jit.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2limits.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2matching.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2partial.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2pattern.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2perform.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2posix.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2sample.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2serialize.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2syntax.3"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/pcre2unicode.3"
    )
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xUnspecifiedx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/share/doc/pcre2/html" TYPE FILE FILES
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/index.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2-config.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_callout_enumerate.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_code_copy.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_code_copy_with_tables.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_code_free.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_compile.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_compile_context_copy.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_compile_context_create.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_compile_context_free.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_config.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_convert_context_copy.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_convert_context_create.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_convert_context_free.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_converted_pattern_free.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_dfa_match.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_general_context_copy.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_general_context_create.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_general_context_free.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_get_error_message.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_get_mark.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_get_match_data_heapframes_size.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_get_match_data_size.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_get_ovector_count.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_get_ovector_pointer.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_get_startchar.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_jit_compile.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_jit_free_unused_memory.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_jit_match.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_jit_stack_assign.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_jit_stack_create.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_jit_stack_free.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_maketables.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_maketables_free.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_match.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_match_context_copy.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_match_context_create.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_match_context_free.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_match_data_create.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_match_data_create_from_pattern.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_match_data_free.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_pattern_convert.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_pattern_info.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_serialize_decode.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_serialize_encode.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_serialize_free.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_serialize_get_number_of_codes.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_set_bsr.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_set_callout.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_set_character_tables.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_set_compile_extra_options.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_set_compile_recursion_guard.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_set_depth_limit.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_set_glob_escape.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_set_glob_separator.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_set_heap_limit.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_set_match_limit.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_set_max_pattern_compiled_length.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_set_max_pattern_length.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_set_max_varlookbehind.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_set_newline.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_set_offset_limit.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_set_parens_nest_limit.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_set_recursion_limit.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_set_recursion_memory_management.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_set_substitute_callout.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_substitute.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_substring_copy_byname.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_substring_copy_bynumber.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_substring_free.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_substring_get_byname.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_substring_get_bynumber.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_substring_length_byname.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_substring_length_bynumber.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_substring_list_free.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_substring_list_get.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_substring_nametable_scan.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2_substring_number_from_name.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2api.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2build.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2callout.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2compat.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2convert.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2demo.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2grep.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2jit.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2limits.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2matching.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2partial.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2pattern.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2perform.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2posix.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2sample.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2serialize.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2syntax.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2test.html"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/src/doc/html/pcre2unicode.html"
    )
endif()

if(CMAKE_INSTALL_COMPONENT)
  set(CMAKE_INSTALL_MANIFEST "install_manifest_${CMAKE_INSTALL_COMPONENT}.txt")
else()
  set(CMAKE_INSTALL_MANIFEST "install_manifest.txt")
endif()

string(REPLACE ";" "\n" CMAKE_INSTALL_MANIFEST_CONTENT
       "${CMAKE_INSTALL_MANIFEST_FILES}")
file(WRITE "D:/AndroidApp/MaterialFile/materialfile_deps/native/pcre2/build/arm64-v8a/${CMAKE_INSTALL_MANIFEST}"
     "${CMAKE_INSTALL_MANIFEST_CONTENT}")
