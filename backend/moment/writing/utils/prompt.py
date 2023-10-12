class PromptTemplate:
    template: str

    @classmethod
    def get_prompt(cls, **kwargs) -> str:
        return cls.template.format(**kwargs)


# Just an example. Make your own prompt templates like this.
class ExamplePromptTemplate(PromptTemplate):
    template = "Make a short reply for the following diary.\n{moment}\nReply:"
