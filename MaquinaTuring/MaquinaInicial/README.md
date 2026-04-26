# Máquina de Turing

Este proyecto implementa una Máquina de Turing educativa en Java con una interfaz visual basada en `index.html`. El backend transforma cualquier texto en una secuencia binaria ASCII de 8 bits por carácter y simula la ejecución del cabezal sobre una cinta infinita.

---

## Estructura esencial del proyecto

```
MaquinaInicial/
├── src/
│   ├── ConvertidorBinario.java   # Convierte texto en binario ASCII de 8 bits
│   ├── Cinta.java                # Cinta infinita, lectura/escritura y movimiento
│   ├── Transicion.java           # Regla de transición: estado + símbolo -> acción
│   ├── MaquinaTuring.java        # Motor de la Máquina de Turing
│   └── ServidorHttpTuring.java   # Servidor HTTP + API para la interfaz
├── index.html                    # Interfaz visual educativa
└── README.md                     # Documentación en español
```


## Flujo principal de la Máquina de Turing

1. El usuario envía un texto al backend desde la UI.
2. `ConvertidorBinario.convertirABinario()` convierte cada carácter en su representación ASCII de 8 bits.
3. El backend crea la cinta con la secuencia de bits y carga la máquina en el estado inicial `q0`.
4. Cada paso se ejecuta con `/api/step` y devuelve el estado actual, el bit leído, la regla aplicada y la posición del cabezal.
5. La UI muestra la cinta, el diccionario binario y el registro paso a paso.

---

## Conversión de letras y números a binario

### ¿Cómo convierte la máquina el texto?

- Cada carácter se transforma en su código ASCII.
- Cada código ASCII se convierte en una cadena binaria de 8 bits.
- El resultado es una secuencia de bits concatenada.

### Ejemplos

| Entrada | ASCII | Binario 8 bits |
|--------|-------|----------------|
| `A` | 65 | `01000001` |
| `B` | 66 | `01000010` |
| `0` | 48 | `00110000` |
| `9` | 57 | `00111001` |
| `a` | 97 | `01100001` |

### Resultado en cinta

Para el texto `AB`, la cinta recibe:
```
0100000101000010
```
Cada carácter aparece como 8 bits consecutivos.
---

##  Componentes de la Máquina de Turing

### `ConvertidorBinario.java`
- Convierte texto a binario ASCII de 8 bits.
- Mantiene el alfabeto de entrada en `0` y `1`.

### `Cinta.java`
- Simula una cinta infinita usando `HashMap<Integer, Character>`.
- Gestiona la lectura, escritura y movimiento del cabezal.
- Usa el símbolo `VACIO` (`B`) para posiciones vacías.

### `Transicion.java`
- Define una regla de transición completa:
  - estado actual
  - símbolo leído
  - símbolo escrito
  - movimiento (`L`, `R`, `N`)
  - siguiente estado
- Cada transición es una regla independiente y descrita.

### `MaquinaTuring.java`
- Contiene la lógica de ejecución de la máquina.
- Ejecuta un paso con la regla válida para el estado y el símbolo actual.
- Mantiene el estado actual, contador de pasos y lista de transiciones.

### `ServidorHttpTuring.java`
- Administra la API HTTP.
- Recibe el texto, crea la máquina y expone endpoints para iniciar, reiniciar y avanzar pasos.
- Devuelve información paso a paso para la UI.

---

## Estados y ciclo de ejecución

La máquina utiliza un ciclo obligatorio de 4 estados por cada bit:

1. `q0` - Lectura
2. `q1` - Procesamiento
3. `q2` - Escritura/Cálculo
4. `q3` - Verificación y avance

### Estado final
- `q_accept` indica que la máquina terminó correctamente cuando el cabezal encuentra un `VACIO` después de procesar todos los bits.

---

## Tabla de transiciones principales

| ID | Estado actual | Lee | Escribe | Mueve | Siguiente | Propósito |
|----|---------------|-----|---------|-------|-----------|-----------|
| 1  | `q0` | `0` | `0` | `N` | `q1` | Lectura de bit 0 |
| 2  | `q0` | `1` | `1` | `N` | `q1` | Lectura de bit 1 |
| 3  | `q0` | `B` | `B` | `N` | `q_accept` | Fin de entrada |
| 4  | `q1` | `0` | `0` | `N` | `q2` | Procesamiento de 0 |
| 5  | `q1` | `1` | `1` | `N` | `q2` | Procesamiento de 1 |
| 6  | `q1` | `B` | `B` | `N` | `q_accept` | Fin durante procesamiento |
| 7  | `q2` | `0` | `0` | `N` | `q3` | Cálculo de 0 |
| 8  | `q2` | `1` | `1` | `N` | `q3` | Cálculo de 1 |
| 9  | `q2` | `B` | `B` | `N` | `q_accept` | Fin durante cálculo |
| 10 | `q3` | `0` | `0` | `R` | `q0` | Verificación y avance |
| 11 | `q3` | `1` | `1` | `R` | `q0` | Verificación y avance |
| 12 | `q3` | `B` | `B` | `N` | `q_accept` | Fin al verificar |

> `N` significa sin movimiento de cabezal.

---

## Ejemplo paso a paso

### Entrada: `B`
- ASCII: 66
- Binario: `01000010`
- Bits totales: 8

### Resumen de ejecución
- Cada bit recorre 4 estados
- Total de pasos: 8 bits × 4 = 32
- +1 paso final de `VACIO`
- Total final: 33 pasos

### Ciclo para el primer bit (`0`)

1. `q0` lee `0`, no mueve, va a `q1`
2. `q1` procesa `0`, no mueve, va a `q2`
3. `q2` escribe/verifica cálculo, no mueve, va a `q3`
4. `q3` verifica `0`, mueve derecha, va a `q0`

### Ciclo para el segundo bit (`1`)

5. `q0` lee `1`, no mueve, va a `q1`
6. `q1` procesa `1`, no mueve, va a `q2`
7. `q2` escribe/verifica cálculo, no mueve, va a `q3`
8. `q3` verifica `1`, mueve derecha, va a `q0`

... y así hasta procesar los 8 bits.

### Paso final

33. `q0` lee `B` (VACIO), no mueve, va a `q_accept`.

---

## Cómo funciona la cinta y el cabezal

- La cinta almacena bits en posiciones enteras.
- El cabezal lee el símbolo actual con `leer()`.
- El cabezal escribe con `escribir(char c)`.
- El cabezal se mueve con `mover("L")`, `mover("R")` o se queda con `mover("N")`.
- Las posiciones vacías devuelven `VACIO`.

---

## API disponible

### `POST /api/init`
- Inicializa la máquina con el texto enviado.
- Ejemplo body: `{"input":"Hola"}`

- Respuesta incluye:
  - `binary`
  - `headPosition`
  - `rules`
  - `state`
  - `stateInfo`

### `POST /api/reset`
- Reinicia la ejecución usando el último binario cargado.

### `POST /api/step`
- Avanza un paso en la Máquina de Turing.
- Devuelve el estado actualizado de la cinta y el cabezal.

---

## Notas importantes

- El proyecto mantiene el comportamiento exacto del código activo.
- Esta documentación reemplaza múltiples archivos dispersos y concentra toda la información en un solo `README.md`.
- No se modificó ninguna lógica de código existente.

---
