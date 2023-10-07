export class NewAction {
    static readonly type = '[fan] NewAction';

}

export class ActionWithPayload {
    static readonly type = '[fan] ActionWithPayload';

    constructor(public payload: unknown) {
    }

}
