import { Component } from 'react';
import PropTypes from 'prop-types';

let sameDayFormatter = new Intl.DateTimeFormat("en", { hour: 'numeric', minute: 'numeric' });
let defaultFormatter = new Intl.DateTimeFormat();

const ONE_HOUR = 60 * 60 * 1000;
const JUST_NOW = 60 * 1000

let isSameDay = (day1, day2) => 
    day1.getDate() === day2.getDate() &&
    day1.getMonth() == day2.getMonth() &&
    day1.getFullYear() == day2.getFullYear()

export default class PrettyDate extends Component {
    toDisplayString = (date) => {
        let now = new Date();
        let yesterday = new Date();
        let msDiff = now - date;
        yesterday.setDate(yesterday.getDate() - 1);

        if (msDiff < JUST_NOW) {
            return "Just Now";
        }
        else if (msDiff < ONE_HOUR) {
            return Math.round(msDiff/60000) + " minutes ago";
        }
        else if (isSameDay(now, date)) {
            return "Today " + sameDayFormatter.format(date);
        }
        else if (isSameDay(yesterday, date)) {
            return "Yesterday " + sameDayFormatter.format(date);
        }
        else {
            return defaultFormatter.format(date);
        }
    }

    doRender = ({ date, ...props }) =>
    <span title={date.toLocaleString()} {...props}>
        {this.toDisplayString(date)}
    </span>

    // TODO: Make this more efficient?
    render = () => this.doRender({ date: new Date(this.props.date), ...this.props })
}

PrettyDate.propTypes = {
    date: PropTypes.instanceOf(Date).isRequired,
};
