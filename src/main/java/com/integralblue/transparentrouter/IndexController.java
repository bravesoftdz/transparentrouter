package com.integralblue.transparentrouter;

import com.integralblue.transparentrouter.repository.PendingReplyRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


/** The index page shows how many pending replies there are.
 * @author Craig Andrews
 *
 */
@Controller
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class IndexController {
	private final PendingReplyRepository pendingReplyRepository;

	@GetMapping("/")
	public String index(final @NonNull Model model) {
		model.addAttribute("pendingReplyCount", pendingReplyRepository.count());
		return "index";
	}
}
