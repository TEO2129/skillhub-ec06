<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use App\Models\Inscription;
use Illuminate\Support\Facades\Auth;

// Questions 1 : (C21/C22) - Middleware pour mise à jour activité
class UpdateInscriptionActivity
{
    public function handle(Request $request, Closure $next)
    {
        $response = $next($request);

        if (Auth::check() && $request->route('id')) {
            $userId = Auth::id();
            $formationId = $request->route('id');

            Inscription::where('user_id', $userId)
                ->where('formation_id', $formationId)
                ->update(['last_activity_at' => now()]);
        }

        return $response;
    }
}
