export class NewAction {
    static readonly type = '[Fan] NewAction';
}

export class ActionWithPayload {
    static readonly type = '[Fan] ActionWithPayload';

    constructor(public payload: unknown) {
    }
}


