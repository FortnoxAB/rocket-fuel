package impl;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QuestionSearchOptionsTest {

	@Test
	public void shouldExtractSearchString() {
	    QuestionSearchOptions questionSearchOptions = QuestionSearchOptions.from("thing");
	    assertThat(questionSearchOptions.getContentSearch()).isEqualTo("thing");
	    assertThat(questionSearchOptions.getTags()).isEmpty();
	}

	@Test
    public void shouldExtractTags() {
        QuestionSearchOptions questionSearchOptions = QuestionSearchOptions.from("[tag]");
        assertThat(questionSearchOptions.getContentSearch()).isEqualTo("");
        assertThat(questionSearchOptions.getTags())
            .containsExactly("tag");
    }

    @Test
    public void shouldExtractSearchStringAndTags() {
        QuestionSearchOptions questionSearchOptions = QuestionSearchOptions.from("[tag1] thing1 [tag2] [tag3] thing2");
        assertThat(questionSearchOptions.getContentSearch()).isEqualTo("thing1 thing2");
        assertThat(questionSearchOptions.getTags())
            .containsExactlyInAnyOrder("tag1", "tag2", "tag3");
    }
}
