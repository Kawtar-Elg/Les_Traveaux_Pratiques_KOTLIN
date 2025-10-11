package Tp14_1

import kotlinx.coroutines.*



suspend fun verifierDisponibilite(): Boolean {
    println("Vérification des ingrédients en cours...")
    delay(2000)
    println("Ingrédients disponibles !")
    return true
}


suspend fun preparerCommande(): String {
    println("Préparation de la commande en cours...")
    delay(5000)
    println("Commande prête !")
    return "Pizza Margherita"
}


suspend fun livrerRepas(nomCommande: String): String {
    return withContext(Dispatchers.IO) {
        println("Début de la livraison de $nomCommande...")
        delay(3000)
        println("Livraison terminée !")
        "Livraison réussie : $nomCommande"
    }
}

fun main() = runBlocking {
        val disponible = verifierDisponibilite()
        if (disponible) {
            val commande = preparerCommande()

            val resultatLivraison = livrerRepas(commande)
            println(resultatLivraison)
        } else {
            println("Ingrédients indisponibles. Commande annulée.")
        }
    }
