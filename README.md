# Тестовое задание в Deeplay

Пусть n - сторона игрового поля.

Рассмотрим два случая:

1) Пусть у нас нет отрицательных стоймостей перемещения, тогда для поля 4 на 4 (а размер у нас фиксированный, даже для пункта со звёздочкой) невозможны ситуации, когда путь с минимальными затратами содержал бы шаги вверх или влево. Значит мы идём только вправо или вниз, можем использовать динамику для нахождения ответа. Будет работать за O(n²)
2) Пусть отрицательные стоймости перемещения могут быть, тогда используем алгоритм Форда-Беллмана. Будет работать за O(n⁴), но в среднем по тестам даже O(n²)

Если бы длина поля могла быть иной, то для первого случая можно было бы использовать Дейкстру или A*, они будут работать за O(n² log n), но во многих частных случаях A* будет быстрее. Для второго случая ничего лучше не знаю.

Для проверки полученного ответа в тестах использую полный перебор.
