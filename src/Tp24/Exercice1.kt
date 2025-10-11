import kotlinx.coroutines.*

fun main() = runBlocking {

    val job = Job()
    val scope = CoroutineScope(Dispatchers.Default + job)


    scope.launch {
        delay(500L)
        println("Coroutine 1 : une erreur s’est produite !")
        throw Exception("Erreur dans la coroutine 1 ")
    }

    scope.launch {
        try {
            delay(1000L)
            println("Coroutine 2 : tâche terminée avec succès !")
        } catch (e: CancellationException) {
            println("Coroutine 2 annulée à cause de l’échec de la coroutine 1")
        }
    }



    println("Fin du programme.")



    supervisorScope {
        val job1 = launch {
            try {
                delay(1000)
                throw Exception("Erreur dans la coroutine 1")
            } catch (e: Exception) {
                println("Coroutine 1 : une erreur s’est produite !")
            }
        }

        val job2 = launch {
            try {
                delay(2000)
                println("Coroutine 2 terminée")
            } catch (e: CancellationException) {
                println("Coroutine 2 annulée à cause de l’échec de la coroutine 1")
            }
        }

        joinAll(job1, job2)
    }
    println("Fin du programme.")
}