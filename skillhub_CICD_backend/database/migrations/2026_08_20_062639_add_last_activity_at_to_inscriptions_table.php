<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        // Questions 1 correction (C21/C22) - Migration
        Schema::table('inscriptions', function (Blueprint $table) {
            if (!Schema::hasColumn('inscriptions', 'last_activity_at')) {
                $table->dateTime('last_activity_at')->nullable();
                $table->index('last_activity_at');
            }
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        // Questions 1 : (C21/C22) - Rollback migration
        Schema::table('inscriptions', function (Blueprint $table) {
            if (Schema::hasColumn('inscriptions', 'last_activity_at')) {
                $table->dropColumn('last_activity_at');
            }
        });
    }
};
