# Install script for directory: D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include

# Set the install prefix
if(NOT DEFINED CMAKE_INSTALL_PREFIX)
  set(CMAKE_INSTALL_PREFIX "D:/AndroidApp/MaterialFile/prebuild/native/mbedtls")
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
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/include/mbedtls" TYPE FILE PERMISSIONS OWNER_READ OWNER_WRITE GROUP_READ WORLD_READ FILES
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/aes.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/aria.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/asn1.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/asn1write.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/base64.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/bignum.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/block_cipher.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/build_info.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/camellia.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/ccm.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/chacha20.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/chachapoly.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/check_config.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/cipher.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/cmac.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/compat-2.x.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/config_adjust_legacy_crypto.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/config_adjust_legacy_from_psa.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/config_adjust_psa_from_legacy.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/config_adjust_psa_superset_legacy.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/config_adjust_ssl.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/config_adjust_x509.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/config_psa.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/constant_time.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/ctr_drbg.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/debug.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/des.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/dhm.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/ecdh.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/ecdsa.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/ecjpake.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/ecp.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/entropy.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/error.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/gcm.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/hkdf.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/hmac_drbg.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/lms.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/mbedtls_config.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/md.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/md5.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/memory_buffer_alloc.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/net_sockets.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/nist_kw.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/oid.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/pem.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/pk.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/pkcs12.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/pkcs5.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/pkcs7.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/platform.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/platform_time.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/platform_util.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/poly1305.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/private_access.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/psa_util.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/ripemd160.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/rsa.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/sha1.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/sha256.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/sha3.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/sha512.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/ssl.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/ssl_cache.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/ssl_ciphersuites.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/ssl_cookie.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/ssl_ticket.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/threading.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/timing.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/version.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/x509.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/x509_crl.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/x509_crt.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/mbedtls/x509_csr.h"
    )
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xUnspecifiedx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/include/psa" TYPE FILE PERMISSIONS OWNER_READ OWNER_WRITE GROUP_READ WORLD_READ FILES
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/build_info.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto_adjust_auto_enabled.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto_adjust_config_dependencies.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto_adjust_config_key_pair_types.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto_adjust_config_synonyms.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto_builtin_composites.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto_builtin_key_derivation.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto_builtin_primitives.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto_compat.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto_config.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto_driver_common.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto_driver_contexts_composites.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto_driver_contexts_key_derivation.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto_driver_contexts_primitives.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto_extra.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto_legacy.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto_platform.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto_se_driver.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto_sizes.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto_struct.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto_types.h"
    "D:/AndroidApp/MaterialFile/materialfile_deps/native/mbedtls/src/include/psa/crypto_values.h"
    )
endif()

