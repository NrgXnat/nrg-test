package org.nrg.testing.email

import com.google.api.services.gmail.model.Message

import java.util.function.Consumer

class EmailQueryResult extends ArrayList<ParsedEmail> {

    EmailQueryResult(List<Message> list) {
        super(list.collect {
            ParsedEmail.fromId(it.getId())
        })
    }

    EmailQueryResult validateResults(Consumer<List<ParsedEmail>> validator) {
        validator.accept(this)
        this
    }

    EmailQueryResult validateEach(Consumer<ParsedEmail> validator) {
        this.each { message ->
            validator.accept(message)
        }
    }

    ParsedEmail getEmail() {
        get(0)
    }

}
