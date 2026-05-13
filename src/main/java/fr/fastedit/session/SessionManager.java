package fr.fastedit.session;

import cn.nukkit.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();
    public static SessionManager get() { return INSTANCE; }

    private final ConcurrentHashMap<UUID, Session> sessions = new ConcurrentHashMap<>();

    public Session of(Player p) {
        return sessions.computeIfAbsent(p.getUniqueId(), Session::new);
    }

    public Session ofOrNull(UUID uuid) { return sessions.get(uuid); }

    public void forget(UUID uuid) { sessions.remove(uuid); }
}
