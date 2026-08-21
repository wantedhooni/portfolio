/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revy.batch.chapter06.domain.support

import com.revy.batch.chapter06.domain.Transaction
import com.revy.batch.chapter06.domain.TransactionDao
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import javax.sql.DataSource

/**
 * @author Michael Minella
 */
class TransactionDaoSupport(dataSource: DataSource) : JdbcTemplate(dataSource), TransactionDao {
    override fun getTransactionsByAccountNumber(accountNumber: String?): MutableList<Transaction?>? {
        return query<Transaction?>(
            "select t.id, t.timestamp, t.amount " +
                    "from transaction t inner join account_summary a on " +
                    "a.id = t.account_summary_id " +
                    "where a.account_number = ?",
            arrayOf<Any?>(accountNumber),
            RowMapper { rs: ResultSet?, rowNum: Int ->
                val trans = Transaction()
                trans.amount = rs!!.getDouble("amount")
                trans.timestamp = rs.getDate("timestamp")
                trans
            }
        )
    }
}
