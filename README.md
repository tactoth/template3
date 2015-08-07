# template3
A simple template engine for generating source code.

How to use?

- Create templates

        $for(field in fields)
        $(field.first) $(field.second);$end()


- Apply params

        template3 gen -Dfields=":map<name:TextView,age:Slider>" -template android_activity_fields

That's it, enjoy!
