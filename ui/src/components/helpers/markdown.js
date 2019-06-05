import React from 'react';
import ReactMarkdown from 'react-markdown';
import CodeBlock from './codeblock';

class Markdown extends React.Component {
    render() {
        return (
            <div className="markdown">
                <ReactMarkdown
                    source={this.props.text}
                    renderers={{ code: CodeBlock }}
                />
            </div>
        );
    }
}

Markdown.defaultProps = {
    text: ''
};

export default Markdown;
