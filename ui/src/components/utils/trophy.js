import React from 'react';
import PropTypes from 'prop-types';

const Trophy = (props) => (
    <span className={`trophy ${props.active ? 'active' : ''}`}>
        <i className="fa fa-trophy" />
    </span>
);

Trophy.defaultProps = {
    active: false
};

Trophy.propTypes = {
    active: PropTypes.bool
};

export default Trophy;
