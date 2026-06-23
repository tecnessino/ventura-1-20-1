package lol.ventura.misc.request;

import lol.ventura.foundation.module.Module;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.PriorityQueue;

import static java.util.Comparator.comparing;

public class RequestHandler<T> {
    private final PriorityQueue<Request<T>> activeRequests = new PriorityQueue<>(comparing((Request<T> r) -> r.getPriority()).reversed());

    public void tick(int deltaTime) {
        activeRequests.forEach(request -> request.setExpiresIn(Math.max(request.getExpiresIn() - deltaTime, 0)));
        activeRequests.removeIf(request -> request.getExpiresIn() <= 0 || !request.getProvider().isEnabled());
    }

    public void request(Request<T> request) {
        activeRequests.removeIf(existingRequest -> existingRequest.getProvider() == request.getProvider());
        activeRequests.add(request);
    }

    public T getActiveRequestValue() {
        if (activeRequests.isEmpty())
            return null;

        return activeRequests.peek().getValue();
    }

    @Data
    @AllArgsConstructor
    public static class Request<T> {
        private int expiresIn;
        private final int priority;
        private final Module provider;
        private final T value;
    }
}