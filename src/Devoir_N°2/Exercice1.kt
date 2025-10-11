import kotlinx.coroutines.*
import kotlin.random.Random


data class Commande(val id: Int, val plats: List<String>, val total: Double) {
    fun afficherDetails() = println("Commande $id : ${plats.joinToString(", ")} | Total: $total €")
}

class Serveur(val nom: String) {
    fun prendreCommande(plats: List<String>, prix: List<Double>): Commande {
        val total = prix.sum()
        val id = plats.hashCode().absoluteValue
        return Commande(id, plats, total)
    }

    fun afficherCommande(commandes: List<Commande>) {
        println("Commandes prises par $nom :")
        commandes.forEach { it.afficherDetails() }
    }
}

class Cuisinier(val nom: String) {
    suspend fun preparerPlat(plat: String): String {
        delay(1000 + Random.nextInt(1000).toLong()) // Simule le temps de préparation
        return "$plat prêt par $nom"
    }

    suspend fun preparerCommande(commande: Commande): List<String> = coroutineScope {
        commande.plats.map { plat ->
            async { preparerPlat(plat) }
        }.awaitAll()
    }
}

class Cuisine(val cuisiniers: List<Cuisinier>) {
    suspend fun gererPreparationCommande(commande: Commande): List<String> = coroutineScope {
        val jobs = cuisiniers.mapIndexed { index, cuisinier ->
            val platsPourCuisinier = commande.plats.chunked((commande.plats.size + cuisiniers.size - 1) / cuisiniers.size)
                .getOrNull(index) ?: emptyList()
            async {
                platsPourCuisinier.map { plat -> cuisinier.preparerPlat(plat) }
            }
        }
        jobs.awaitAll().flatten()
    }
}


class Caisse {
    suspend fun traiterPaiement(commande: Commande): Boolean {
        delay(500)
        if (Random.nextBoolean()) {
            println("Paiement accepté pour la commande ${commande.id}")
            return true
        } else {
            throw Exception("Échec du paiement pour la commande ${commande.id}")
        }
    }

    fun annulerPaiement(job: Job) {
        job.cancel("Paiement annulé par le client")
        println("Paiement annulé.")
    }
}

class Restaurant(
    val serveurs: List<Serveur>,
    val cuisine: Cuisine,
    val caisse: Caisse
) {
    private val commandes = mutableListOf<Commande>()

    suspend fun prendreCommandeEtTraiter(
        serveur: Serveur,
        plats: List<String>,
        prix: List<Double>
    ): Job = CoroutineScope(Dispatchers.Default).launch {
        val commande = serveur.prendreCommande(plats, prix)
        commandes.add(commande)
        println("Commande prise : ${commande.id}")

        try {
            val resultats = cuisine.gererPreparationCommande(commande)
            resultats.forEach { println("$it") }

            val paiementOk = caisse.traiterPaiement(commande)
            if (paiementOk) {
                println("Commande ${commande.id} terminée avec succès !")
            }
        } catch (e: Exception) {
            println("Erreur : ${e.message}")
        }
    }

    fun afficherCommandesEnCours() {
        println("Commandes en cours : ${commandes.size}")
        commandes.forEach { it.afficherDetails() }
    }
}

fun main() = coroutineScope {
    val cuisiniers = listOf(Cuisinier("Ali"), Cuisinier("Samia"))
    val cuisine = Cuisine(cuisiniers)
    val caisse = Caisse()
    val serveurs = listOf(Serveur("Youssef"))

    val restaurant = Restaurant(serveurs, cuisine, caisse)

    val job = restaurant.prendreCommandeEtTraiter(
        serveur = serveurs[0],
        plats = listOf("Pizza", "Salade", "Dessert"),
        prix = listOf(12.0, 6.0, 4.0)
    )

    delay(2000)
    job.join()
    restaurant.afficherCommandesEnCours()
}

