'''
*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
*   IMPORTS
*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
'''

import time  # Per Timer --> tempo di esecuzione
import copy  # Per fare copie profonde di variabili

'''
*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
*   UTILITIES
*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
'''


def print_matrix(m):
    """ Utility per stampa della matrice """
    for i in range(len(m[0])):
        print(m[i])


def utility_explain_results(base_matrix):
    print("Matrice di base")
    print_matrix(base_matrix)
    print("-------------------------")
    print("Matrice ruotata di 90°")
    print_matrix(rotaded_base_matrix)
    print("-------------------------")
    print("Pattern da cercare", pattern)
    print("-------------------------")
    print("Pattern invertito", inverse_pattern)
    print("-------------------------")
    print("RISULTATI")


'''
*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
*   VARIABILI GLOBALI
*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
'''

density_coords = []
inv_density_coords = []

'''
*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
*   ANALISI DELL'INPUT E ESTRAPOLAZIONE DEL PATTERN
*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
'''


def find_pattern_from_input_matrix(input_matrix):
    """ Estrapolazione di un pattern dall'input dell'utente """
    result_pattern = []
    for row in input_matrix:
        row_pattern = []
        row_pattern = find_pattern_from_list(row, row_pattern)
        # Ignoro le liste del tipo [0,0,0,0,0] all'inizio
        if result_pattern or len(row_pattern) != 1:
            result_pattern.append(row_pattern)
    return result_pattern


def find_pattern_from_list(input_list, results, pos=0, previous_val='unset', ones=0, zeros=0):
    """ Estrapolazione di una porzione di pattern per ciascuna riga/lista di input
    Esempio:
     lista      [1, 1, 1, 0, 1, 0, 0]
     pattern    [0, 3, 1, 1]
    N.B. Il pattern parte sempre dalla conta degli zero iniziali e ignora gli zero finali"""
    # INSERIMENTO VALORE PRECEDENTE
    # Se c'è stata una precedente iterazione E l'elemento attuale è diverso dal valore precedente...
    if previous_val != 'unset' and input_list[pos] != previous_val:
        # Se il valore precedente era 1, lo inserisco nei risultati
        if previous_val == 1:
            # Il primo valore nella sublist rappresenta sempre il numero di zero iniziali (anche se nullo)
            if not results:
                results.append(0)
            results.append(ones)
            ones = 0
        # Se il valore precedente era 0, lo inserisco nei risultati
        else:
            results.append(zeros)
            zeros = 0

    # ANALISI VALORE CORRENTE
    if input_list[pos] == 1:
        # Se è l'ultimo della lista, lo inserisco e restituisco i risultati
        if pos + 1 == len(input_list):
            if not results:
                results.append(0)
            results.append(ones + 1)
            return results
        # Aumento la conta degli uno
        return find_pattern_from_list(input_list, results, pos + 1, input_list[pos], ones=ones + 1, zeros=0)
    else:
        # Sè è l'ultimo numero (zero) della lista, restituisco i risultati
        if pos + 1 == len(input_list):
            if not results:
                results.append(len(input_list))
            return results
        # Aumento la conta degli zero
        return find_pattern_from_list(input_list, results, pos + 1, input_list[pos], ones=0, zeros=zeros + 1)


def clean_pattern(p):
    """ Rimozione liste vuote alla fine del pattern """
    l = len(p) - 1
    while l > 1 and len(p[l]) == 1:
        p.pop()
        l -= 1


def get_extra_zeros(p):
    """ Calcolo zero iniziali in eccesso su tutte le righe del pattern """
    min_zeros = p[0][0]
    for row in p:
        if row[0] < min_zeros:
            min_zeros = row[0]
    return min_zeros


def get_representative_rows(pattern_param):
    """ Individuazione di:
    - lista più lunga del pattern                   max_lenght_row = [coordinata di riga nel pattern, lunghezza]
    - lista più ricca di 1                          max_density_row = [riga nel pattern, lunghezza, densità]
    - lista più ricca di 1 nel pattern inverso      inverse_max_density_row = [riga nel pattern, lunghezza]
    """
    global max_lenght_row, max_density_row, inverse_max_density_row

    # Calcolo zero iniziali in eccesso su tutte le righe del pattern
    min_zeros = get_extra_zeros(pattern_param)

    max_lenght_row = [0, 0]
    max_density_row = [0, 0, 0]
    for i, row in enumerate(pattern_param):
        lenght_sum = 0
        density_sum = 0
        # Rimozione zero iniziali in eccesso
        row[0] -= min_zeros
        for y, el in enumerate(row):
            lenght_sum += el
            if y != 0 and y % 2 != 0:
                density_sum += el
        if lenght_sum > max_lenght_row[1]:
            max_lenght_row[0] = i
            max_lenght_row[1] = lenght_sum
        if density_sum > max_density_row[2]:
            max_density_row[0] = i
            max_density_row[1] = lenght_sum
            max_density_row[2] = density_sum
    # Rimozione ultimo elemento (densità)
    max_density_row.pop()

    # Calcolo coordinate della lista più densa nel pattern invertito (per la ricerca nella matrice ruotata di 180°)
    # [Altezza invertita, lunghezza invariata]
    inverse_max_density_row = [len(pattern_param) - max_density_row[0] - 1, max_density_row[1]]


def get_inverse_pattern(pattern):
    """ Elaborazione pattern invertito (corrisponde all'input dell'utente ruotato di 180°) """
    global inverse_pattern
    inverse_pattern = []

    # Inversione delle sottoliste del pattern
    for row in reversed(pattern):
        sublist = []
        sum = 0
        # Inversione degli elementi nelle sottoliste
        for i, el in enumerate(reversed(row)):
            sum += el
            # Gli zero iniziali diventano zero finali, quindi vanno ignorati
            if i == len(row) - 1:
                continue
            sublist.append(el)
        # Calcolo zero iniziali della sottolista e inserimento in prima posizione
        sublist.insert(0, max_lenght_row[1] - sum)
        # Inserimento nel pattern invertito
        inverse_pattern.append(sublist)


'''
*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
*   RICERCA DEL PATTERN NELLA MATRICE
*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
'''


def rotate_matrix_90(m):
    """ Rotazione della matrice di 90° """
    len_m = len(m[0])
    for i in range(len_m // 2):
        for j in range(i, len_m - i - 1):
            temp = m[i][j]
            m[i][j] = m[len_m - 1 - j][i]
            m[len_m - 1 - j][i] = m[len_m - 1 - i][len_m - 1 - j]
            m[len_m - 1 - i][len_m - 1 - j] = m[j][len_m - 1 - i]
            m[j][len_m - 1 - i] = temp


def search_pattern(base_matrix):
    """ Funzione madre di ricerca del pattern nella matrice """
    global max_lenght_row
    global rotaded_base_matrix

    # RICERCA DEL PATTERN (Matrice di base e ruotata di 180°)
    if len(base_matrix[0]) >= max_lenght_row[1]:
        base_results, results_180 = search_per_matrix_rotation(base_matrix)
    else:
        base_results, results_180 = False

    # RICERCA DEL PATTERN (Matrice ruotata di 90° e 270°)
    if len(base_matrix) >= max_lenght_row[1]:
        rotaded_base_matrix = copy.deepcopy(base_matrix)
        rotate_matrix_90(rotaded_base_matrix)
        results_90, results_270 = search_per_matrix_rotation(rotaded_base_matrix)
    else:
        results_90, results_270 = False

    return base_results, results_180, results_90, results_270


def search_per_matrix_rotation(var_matrix):
    """ Ricerca del pattern in ogni orientamento """
    density_coords.clear()
    inv_density_coords.clear()

    # RICERCA DELLE RIGHE PIU' DENSE
    for coord_row, row in enumerate(var_matrix):
        # Ricerca max_density_roww
        # Restrizione ricerca in funzione della coordinata della max_density_row e della lunghezza massima del pattern
        if max_density_row[0] <= coord_row <= len(var_matrix) - len(pattern) + max_density_row[0]:
            search_density_coords(row, pattern[max_density_row[0]], max_density_row[1], coord_row, max_lenght_row[1])
        # Ricerca inverse_max_density_row
        if inverse_max_density_row[0] <= coord_row <= len(var_matrix) - len(pattern) + inverse_max_density_row[0]:
            search_density_coords(row, inverse_pattern[inverse_max_density_row[0]], inverse_max_density_row[1],
                                  coord_row, max_lenght_row[1], True)

    # RICERCA DEL PATTERNO COMPLETO per ciascuna occorrenza delle density_row
    standard_result = check_full_pattern(density_coords, pattern, max_density_row, var_matrix)
    inv_result = check_full_pattern(inv_density_coords, inverse_pattern, inverse_max_density_row, var_matrix)
    return standard_result, inv_result


def search_density_coords(matrix_row, pattern_list, pattern_lenght, i, max_lenght,
                          inverse=False, pos=0, default_lenght=0, last_start=0, match_counter=0, pattern_index=0):
    """  Ricerca della lista più densa del pattern all'interno della matrice
    :param matrix_row:
        lista/riga in matrice
    :param pattern_list:
        lista di lunghezze da cercare, es. [0,3,1,1] <-- [0, 1, 1, 1, 0, 1]
    :param pattern_lenght:
        lunghezza residua da cercare, es. 2 = 1+1
    :param i:
        posizione riga in matrice
    :param max_lenght:
        lunghezza massima dell'intero pattern
    :param inverse:
        flag per riconoscere quando cercare le inverse_max_density_coords
        default = False
    :param pos:
        posizione elemento in riga
        default = 0
    :param default_lenght:
        lunghezza iniziale del pattern da cercare, nell'esempio 5
        default = 0
    :param last_start:
        ultima posizione di avvio, necessaria per avvio ricorsivo su singola riga
        default = 0
    :param match_counter:
        numero di cooccorrenze riscontrate
        default = 0
    :param pattern_index:
        indice all'interno del pattern_list: es. se == 1 --> pattern_list[pattern_index] = 3
        default = 0
    :return:
    """
    # Se devo iniziare o ricominciare a cercare il pattern...
    if default_lenght == 0:
        default_lenght = pattern_lenght
        last_start = pos
    # Controllo se devo smettere di cercare perché
    # (1) ho esaurito il pattern o
    # (2) la lunghezza residua in matrice non è più sufficiente per contenerlo
    if pattern_lenght <= 0 or pattern_lenght > len(matrix_row) - pos:
        return

    if pattern_index == 0 or pattern_index % 2 == 0:
        # Se cerco x volte 0, mi sposto di x nella matrice di base e aumento il pattern_index di 1
        return search_density_coords(matrix_row, pattern_list, pattern_lenght - pattern_list[pattern_index], i,
                                     max_lenght, inverse,
                                     pos + pattern_list[pattern_index], default_lenght, last_start,
                                     pattern_index=pattern_index + 1)

    # Se il valore in matrice è ZERO, riavvio la funzione dall'ultima posizione di avvio
    if matrix_row[pos] == 0:
        if pattern_index != 0:
            # Ricomincio a cercare la sequenza a partire dalla posizione di ultimo avvio in matrice
            return search_density_coords(matrix_row, pattern_list, default_lenght, i, max_lenght, inverse,
                                         pos=last_start + 1)
        # Ricomincio a cercare la sequenza dalla medesima posizione in matrice
        return search_density_coords(matrix_row, pattern_list, default_lenght, i, max_lenght, inverse, pos)

    if pattern_lenght == 1:  # Ultimo elemento del pattern
        if len(matrix_row) - (pos + 1 - default_lenght) >= max_lenght:
            if inverse:
                inv_density_coords.append([i, pos])
            else:
                density_coords.append([i, pos])
        # Se la differenza tra la lunghezza della matrice e la posizione di ritorno sarà maggiore della lunghezza massima del pattern, rilancio la funzione
        # In altri termini, controllo preventivamente se potrò cercare nuovamente il pattern
        # N.B. Ciò che conta qui non è più la lunghezza residua da cercare, bensì la lunghezza massima del pattern!!!!
        if len(matrix_row) - (pos + 1 - default_lenght) + 1 >= max_lenght:
            return search_density_coords(matrix_row, pattern_list, default_lenght, i, max_lenght, inverse,
                                         pos=pos - default_lenght + 2)
        return
    else:  # Pattern non ancora concluso
        # Se ho trovato tutti gli elementi dello stesso valore (ad es., i primi tre 1)
        # resetto il contatore e aumento l'indice della pattern_list
        if pattern_list[pattern_index] == match_counter + 1:
            return search_density_coords(matrix_row, pattern_list, pattern_lenght - 1, i, max_lenght, inverse, pos + 1,
                                         default_lenght, last_start=last_start, pattern_index=pattern_index + 1)
        # Incremento il contatore
        return search_density_coords(matrix_row, pattern_list, pattern_lenght - 1, i, max_lenght, inverse, pos + 1,
                                     default_lenght, last_start=last_start, match_counter=match_counter + 1,
                                     pattern_index=pattern_index)


def check_full_pattern(density_coords, pattern, max_dens_row, b_matrix):
    """ Ricerca del pattern completo per ciascuna occorrenza della lista più densa """
    results = []
    if density_coords:
        for c in density_coords:
            c_result = []
            for row in range(0, len(pattern)):
                row_index = c[0] - max_dens_row[0] + row
                col_index = c[1] - max_dens_row[1] + 1
                c_result.append((row_index, col_index))
                if row_index == c[0]:
                    continue
                if not check_row(b_matrix, pattern[row], row_index, col_index):
                    c_result.clear()
                    break
            if c_result:
                results.append(c_result)
    return results


def check_row(b_matrix, pattern, row_index, col_index, pattern_index=0):
    """
    Ricerca singola riga/lista del pattern. Funzione richiamata da checkFullPattern (v. dopo)
    :param b_matrix:
        matrice di ricerca
    :param pattern:
        singola riga/lista del pattern, che rappresenta la lunghezza di ciascuna alternanza di 0 e 1; es. [0,1,1,3] = zero 1, un 1, uno 0, tre 1
    :param row_index:
        indice di riga nella matrice di ricerca
    :param col_index:
        indice di colonna nella matrice di ricerca
    :param pattern_index:
        indice nella riga/lista del pattern
        incrementa progressivamente di 1
    :return:
        True/False
    """
    # Se ho finito di cercare la singola riga/lista del pattern
    if pattern_index >= len(pattern):
        return True
    # Se cerco 0
    if pattern_index == 0 or pattern_index % 2 == 0:
        col_index = col_index + pattern[pattern_index]
    else:
        # Se cerco 1
        for ones in range(0, pattern[pattern_index]):
            # Se trovo 0 nella matrice di ricerca, smetto di cercare il pattern
            if b_matrix[row_index][col_index] == 0:
                return False
            # Se trovo 1, mi sposto in avanti di una colonna nella matrice di ricerca
            col_index += 1
    # Rilancio la funzione aumentando il pattern_index
    pattern_index += 1
    return check_row(b_matrix, pattern, row_index, col_index, pattern_index)


'''
*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
*   MAIN FUNCTION
*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
'''


def main(base_matrix, pattern_matrix):
    global pattern, inverse_pattern, max_density_row, inverse_max_density_row, max_lenght_row

    # Verifica input e matrice
    if not base_matrix or not base_matrix[0] or not pattern_matrix:
        return "Ops! Qualcosa è andato storto. Inserire un pattern"

    # Ricerca del pattern nell'input
    pattern = find_pattern_from_input_matrix(pattern_matrix)
    # Rimozione liste vuote alla fine del pattern
    clean_pattern(pattern)

    # Ricerca nel pattern di max_density_row, inverse_max_density_row e max_lenght_row
    # Si tratta di liste rappresentative che orientano la ricerca
    get_representative_rows(pattern)

    # Verifica lunghezza del pattern
    if len(base_matrix[0]) < max_lenght_row[1] and len(base_matrix) < max_lenght_row[1]:
        return "Il pattern eccede la grandezza della matrice"

    # Calcolo pattern inverso (per la matrice ruotata di 180°)
    get_inverse_pattern(pattern)

    # Ricerca del pattern nella matrice
    base_results, results_180, results_90, results_270 = search_pattern(base_matrix)

    # Output risultati
    if base_results or results_90 or results_180 or results_270:
        utility_explain_results(base_matrix)
        return f"Occorrenze con matrice di base e pattern di base:\n{base_results}" \
               f"\nOccorrenze con matrice di base e pattern invertito:\n{results_180}" \
               f"\nOccorrenze con matrice ruotata di 90° e pattern di base):\n{results_90}" \
               f"\nOccorrenze con matrice ruotata di 90° e pattern invertito:\n{results_270}"
    else:
        return "Pattern non trovato"


'''
*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
* DATI
*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
'''

pattern_matrix = [[0, 0, 0, 0],
                  [0, 1, 0, 0],
                  [1, 0, 1, 1],
                  [1, 0, 0, 0],
                  [1, 0, 0, 0]]

example_matrix = [[1, 0, 1, 1, 1, 0, 0],
                  [0, 0, 1, 1, 1, 1, 0],
                  [1, 0, 1, 0, 0, 1, 1],
                  [0, 0, 1, 1, 0, 1, 0],
                  [0, 0, 1, 1, 1, 1, 0],
                  [1, 1, 1, 1, 0, 1, 0],
                  [1, 0, 1, 1, 1, 1, 1]]

pattern_matrix1 = [[0, 1, 1, 1, 0],
                   [1, 0, 0, 0, 1],
                   [0, 0, 1, 0, 0],
                   [0, 0, 1, 0, 0],
                   [0, 0, 1, 0, 0],
                   [0, 0, 0, 0, 0]]

#                  0  1  2  3  4  5  6  7
example_matrix1 = [[0, 1, 1, 1, 1, 1, 1, 1],
                   [0, 0, 1, 1, 1, 1, 0, 0],
                   [0, 0, 1, 1, 1, 0, 0, 0],
                   [0, 1, 0, 1, 0, 1, 0, 0],
                   [0, 0, 0, 1, 0, 0, 1, 0],
                   [1, 0, 0, 1, 0, 0, 1, 1],
                   [0, 1, 0, 1, 0, 1, 1, 1],
                   [0, 0, 1, 1, 1, 0, 0, 0]]

pattern_matrix2 = [[0, 0, 1, 1, 0, 0, 0],
                   [0, 1, 0, 0, 0, 0, 0],
                   [0, 0, 0, 1, 0, 1, 0],
                   [0, 0, 0, 1, 0, 0, 0],
                   [0, 0, 0, 1, 0, 0, 0],
                   [0, 0, 0, 0, 0, 0, 0]]

#                   0  1  2  3  4  5  6  7
example_matrix2 = [[0, 1, 1, 1, 1, 1, 1, 1],
                   [0, 0, 1, 1, 1, 1, 0, 0],
                   [0, 0, 1, 1, 1, 0, 0, 0],
                   [0, 1, 0, 0, 0, 1, 0, 0],
                   [0, 0, 0, 1, 0, 1, 1, 0],
                   [1, 0, 0, 1, 0, 0, 1, 1],
                   [1, 0, 0, 1, 0, 0, 1, 1],
                   [0, 0, 0, 1, 0, 0, 1, 1]]

pattern_matrix3 = [[1, 0, 0],
                   [1, 0, 0],
                   [1, 0, 0],
                   [1, 1, 1]]

'''
*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
* LAUNCH
*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
'''

start = time.perf_counter()
print(main(example_matrix, pattern_matrix))
finish = time.perf_counter()
print(f"Tempo di esecuzione: {finish - start:0.8f} secondi")
