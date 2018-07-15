import { Component } from 'react';

let sameDayFormatter = new Intl.DateTimeFormat("en", { hour: 'numeric', minute: 'numeric' });
let defaultFormatter = new Intl.DateTimeFormat();

let isSameDay = (day1, day2) => 
    day1.getDate() === day2.getDate() &&
    day1.getMonth() == day2.getMonth() &&
    day1.getFullYear() == day2.getFullYear()

export default class PrettyDate extends Component {
    toDisplayString = (date) => {
        let now = new Date();
        let yesterday = new Date();
        yesterday.setDate(yesterday.getDate() - 1);

        if (isSameDay(now, date)) {
            return "Today " + sameDayFormatter.format(date);
        }
        else if (isSameDay(yesterday, date)) {
            return "Yesterday " + sameDayFormatter.format(date);
        }
        else {
            return defaultFormatter.format(date);
        }
    }

    doRender = (date, style) =>
    <span title={date.toLocaleString()} style={style}>
        {this.toDisplayString(date)}
    </span>

    // TODO: Make this more efficient?
    render = () => this.doRender(new Date(this.props.date), this.props.style)
}