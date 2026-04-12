/*
 * Copyright (c) 2026 naipingzai <npznnz@gmail.com>
 * All Rights Reserved.
 */

package naipingzai.materialfile.provider.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UriAuthorityTest {

    @Test
    fun empty() {
        val auth = UriAuthority.EMPTY
        assertThat(auth.host).isEmpty()
        assertThat(auth.port).isNull()
        assertThat(auth.userInfo).isNull()
    }

    @Test
    fun toString_hostOnly() {
        val auth = UriAuthority(null, "example.com", null)
        assertThat(auth.toString()).isEqualTo("example.com")
    }

    @Test
    fun toString_hostAndPort() {
        val auth = UriAuthority(null, "example.com", 8080)
        assertThat(auth.toString()).isEqualTo("example.com:8080")
    }

    @Test
    fun toString_withUserInfo() {
        val auth = UriAuthority("user", "example.com", null)
        assertThat(auth.toString()).isEqualTo("user@example.com")
    }

    @Test
    fun toString_full() {
        val auth = UriAuthority("admin", "example.com", 443)
        assertThat(auth.toString()).isEqualTo("admin@example.com:443")
    }

    @Test
    fun encode_simpleHost() {
        val auth = UriAuthority(null, "example.com", null)
        assertThat(auth.encode()).isEqualTo("example.com")
    }

    @Test
    fun encode_hostAndPort() {
        val auth = UriAuthority(null, "example.com", 8080)
        assertThat(auth.encode()).isEqualTo("example.com:8080")
    }

    @Test
    fun encode_withUserInfo() {
        val auth = UriAuthority("user", "example.com", null)
        assertThat(auth.encode()).isEqualTo("user@example.com")
    }

    @Test
    fun encode_empty() {
        val auth = UriAuthority.EMPTY
        assertThat(auth.encode()).isEmpty()
    }

    @Test
    fun equality() {
        val a = UriAuthority("user", "host", 80)
        val b = UriAuthority("user", "host", 80)
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
    }

    @Test
    fun inequality() {
        val a = UriAuthority(null, "a.com", null)
        val b = UriAuthority(null, "b.com", null)
        assertThat(a).isNotEqualTo(b)
    }
}
