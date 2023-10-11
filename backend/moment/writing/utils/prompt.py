class PromptTemplate:
    template: str

    @classmethod
    def get_prompt(cls, **kwargs) -> str:
        return cls.template.format(**kwargs)
