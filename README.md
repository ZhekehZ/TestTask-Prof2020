# TestTask-Prof2020


## Usage:
    :save  [FILE]                   - сохранить текущее состояние в FILE
    :load  [FILE]                   - загрузить текущее состояние из FILE
    :add   [FILE]                   - добавить FILE в структуру
    :files [LINE]                   - получить список файлов, содержащих LINE
    :upd   [FILE] [LINK] [NEW_FILE] - заменить испорченную include-ссылку LINK в файле FILE на NEW_FILE
    :lines                          - получить список обычных строк
    :inv                            - получить список испорченных ссылок
    :inc   [FILE]                   - получить список файлов, непосредствнно включаемых в FILE
