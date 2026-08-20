<?php

namespace Tests\Unit;

use App\Models\Inscription;
use App\Models\User;
use App\Models\Formation;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Artisan;
use Carbon\Carbon;
use Tests\TestCase;

// Questions 1 : (C21/C22) - Test de la commande de désinscription
class DesinscriptionInactiviteTest extends TestCase
{
    use RefreshDatabase;

    #[\PHPUnit\Framework\Attributes\Test]
    public function commande_supprime_les_inscriptions_inactives_de_plus_de_30_jours(): void
    {
        $user = User::factory()->create();
        $formation = Formation::factory()->create();

        // Inscription inactive (31 jours)
        $inactiveInscription = Inscription::factory()->create([
            'user_id' => $user->id,  // Changé de utilisateur_id à user_id
            'formation_id' => $formation->id,
            'last_activity_at' => Carbon::now()->subDays(31),
        ]);

        // Inscription active (5 jours)
        $activeInscription = Inscription::factory()->create([
            'user_id' => $user->id,
            'formation_id' => $formation->id,
            'last_activity_at' => Carbon::now()->subDays(5),
        ]);

        // Inscription sans activité (NULL)
        $nullInscription = Inscription::factory()->create([
            'user_id' => $user->id,
            'formation_id' => $formation->id,
            'last_activity_at' => null,
        ]);

        $exitCode = Artisan::call('app:desinscription-inactivite');

        $this->assertEquals(0, $exitCode);
        $this->assertDatabaseMissing('inscriptions', ['id' => $inactiveInscription->id]);
        $this->assertDatabaseMissing('inscriptions', ['id' => $nullInscription->id]);
        $this->assertDatabaseHas('inscriptions', ['id' => $activeInscription->id]);

        $output = Artisan::output();
        $this->assertStringContainsString('2 inscription(s) supprimée(s) pour inactivité.', $output);
    }

    #[\PHPUnit\Framework\Attributes\Test]
    public function commande_ne_supprime_pas_les_inscriptions_recentes(): void
    {
        $user = User::factory()->create();
        $formation = Formation::factory()->create();

        $recentInscription = Inscription::factory()->create([
            'user_id' => $user->id,
            'formation_id' => $formation->id,
            'last_activity_at' => Carbon::now()->subDays(29),
        ]);

        Artisan::call('app:desinscription-inactivite');

        $this->assertDatabaseHas('inscriptions', ['id' => $recentInscription->id]);
    }

    #[\PHPUnit\Framework\Attributes\Test]
    public function commande_retourne_message_quand_aucune_inscription_inactive(): void
    {
        Artisan::call('app:desinscription-inactivite');

        $output = Artisan::output();
        $this->assertStringContainsString('Aucune inscription inactive à supprimer.', $output);
    }
}
