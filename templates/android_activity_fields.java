// fields
        $for(field in fields)
        $(field.first) $(field.second);$end()

// assign fields
            $for(field in fields)
            $(field.second) = ($(field.first)) findViewById(R.id.$(field.second));$end()

