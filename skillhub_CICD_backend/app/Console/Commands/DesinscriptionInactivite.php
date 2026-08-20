<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;
use App\Models\Inscription; // Changé de Enrollment à Inscription
use Illuminate\Support\Facades\Log;
use Carbon\Carbon;

// Questions 1 : (C21/C22) - Commande de désinscription pour inactivité
class DesinscriptionInactivite extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'app:desinscription-inactivite';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Supprime les inscriptions inactives depuis plus de 30 jours';

    /**
     * Execute the console command.
     */
    public function handle(): int
    {
        // Questions 1 : (C21/C22) - Logique de désinscription
        $threshold = Carbon::now()->subDays(30);
        $this->info("Recherche des inscriptions inactives depuis le : {$threshold}");

        // Récupérer les inscriptions inactives
        $inactiveInscriptions = Inscription::where(function($query) use ($threshold) {
            $query->whereNull('last_activity_at')
                  ->orWhere('last_activity_at', '<', $threshold);
        })->get();

        $count = $inactiveInscriptions->count();

        if ($count === 0) {
            $this->info('Aucune inscription inactive à supprimer.');
            Log::info('DesinscriptionInactivite: Aucune inscription supprimée');
            return Command::SUCCESS;
        }

        // Supprimer les inscriptions inactives
        $deletedCount = $inactiveInscriptions->each->delete()->count();

        $message = "{$deletedCount} inscription(s) supprimée(s) pour inactivité.";
        $this->info($message);
        Log::info("DesinscriptionInactivite: {$message}");

        return Command::SUCCESS;
    }
}
