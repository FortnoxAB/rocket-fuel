import React from 'react';
import PropTypes from 'prop-types';

class Dropdown extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            isOpen: props.isOpen,
            closing: false,
            opening: false,
            left: null,
            top: null
        };
        this.closingTimeout;

        this.resize  = this.resizeEvent.bind(this);
        this.onClick = this.onClickEvent.bind(this);
    }

    componentDidMount() {
        window.addEventListener('resize', this.resize);
        window.addEventListener('mouseup', this.onClick, false);
    }

    componentWillUnmount() {
        window.removeEventListener('resize', this.resize);
        window.removeEventListener('mouseup', this.onClick, false);
    }

    isNodeTagLink(node) {
        return node.tagName.toLowerCase() === 'a';
    }

    onClickEvent(e) {
        if (!this.dropdownNode.parentElement.contains(e.target) || this.isNodeTagLink(e.target)) {
            this.props.close();
        }
    }

    resizeEvent() {
        const dropdown = this.dropdownNode;
        const dropdownParent = this.dropdownNode.parentElement;
        const dropdownParentRectangle = dropdownParent.getBoundingClientRect();
        const offset = dropdownParentRectangle.width / 2;
        dropdownParent.style.position = 'relative';

        if (dropdownParentRectangle.x > (window.innerWidth/2)) {
            dropdown.style.right = `${offset}px`;
            return;
        }
        dropdown.style.left = `${offset}px`;
    }

    componentWillReceiveProps(nextProps) {
        if (this.props.isOpen && !nextProps.isOpen) {
            this.setState({
                isOpen: nextProps.isOpen,
                closing: true,
                opening: false
            });
            this.onTransition();
            return;
        }

        if (nextProps.isOpen) {
            this.resizeEvent();
            clearTimeout(this.closingTimeout);
            this.setState({
                isOpen: nextProps.isOpen,
                closing: false,
                opening: true
            });
            this.onTransition();
        }
    }

    onTransition() {
        this.closingTimeout = setTimeout(() => {
            this.setState({
                closing: false,
                opening: false
            });
        }, 200);
    }

    getClasses() {
        if (this.state.isOpen && this.state.opening) {
            return 'opening';
        }
        if (this.state.isOpen) {
            return 'open';
        }
        if (!this.state.isOpen && this.state.closing) {
            return 'closing';
        }
        return 'closed';
    }

    render() {
        return (
            <div
                className={`dropdown ${this.getClasses()}`}
                ref={(node) => {this.dropdownNode = node;}}
            >
                <div className="content">
                    {this.props.children}
                </div>
            </div>
        );
    }
}

Dropdown.propTypes = {
    children: PropTypes.node.isRequired,
    isOpen: PropTypes.bool,
    close: PropTypes.func
};

Dropdown.defaultProps = {
    isOpen: false,
    close: () => {
    }
};

export default Dropdown;
