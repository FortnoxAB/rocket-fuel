import React from 'react';
import PropTypes from 'prop-types';

class Tooltip extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
          visible: false
        };
    }

    componentDidUpdate() {
        if (this.node) {
            this.checkPosition();
        }
    }

    checkPosition() {
        const bounding = this.node.getBoundingClientRect();
        const width = this.node.offsetWidth;
        const outsideWindowRight = (bounding.right + width/2) > window.innerWidth;
        const outsideWindowLeft = (bounding.left - width/2) < 0;

        if (outsideWindowRight) {
            this.node.style.right = 0;
        }

        if (outsideWindowLeft) {
            this.node.style.left = 0;
        }
    }

    showTooltip() {
        this.setState({
            visible: true
        });
    }

    hideTooltip() {
        this.setState({
            visible: false
        });
    }

    renderTooltipContent() {
        if (!this.state.visible) {
            return null;
        }

        return (
            <span
                className="tooltip"
                ref={(node) => {this.node = node}}
            >
                {this.props.content}
            </span>
        );
    }

    render() {
        return (
            <span
                className="tooltip-wrap"
                onMouseEnter={this.showTooltip.bind(this)}
                onMouseLeave={this.hideTooltip.bind(this)}
            >
                {this.props.children}
                {this.renderTooltipContent()}
            </span>
        );
    }
}

Tooltip.defaultProps = {
    content: null
};

Tooltip.propTypes = {
    content: PropTypes.node
};

export default Tooltip;
