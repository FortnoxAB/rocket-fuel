import React from 'react';
import {withRouter} from 'react-router-dom';

class Tags extends React.Component {
    constructor(props) {
        super(props);
    }

    clickTag(tag) {
        const encodedLabel = encodeURIComponent(`#${tag.label}`);
        this.props.history.push(`/search/${encodedLabel}`);
    }

    renderTags() {
        return this.props.tags.map((tag, i) => {
            return (
                <div key={i} onClick={this.clickTag.bind(this, tag)} className="tag">#{tag.label}</div>
            );
        })
    }

    render() {
        if (!this.props.tags || this.props.tags.length <= 0) {
            return null;
        }
        return (
            <div className="tags">
                {this.renderTags()}
            </div>
        );
    }
}

Tags.defaultProps = {
    tags: []
};

export default withRouter(Tags);
