package com.practice.spring.webflux.demo.service.impl;

import com.practice.spring.webflux.demo.model.ResultModel;
import com.practice.spring.webflux.demo.model.ResultViewModel;
import com.practice.spring.webflux.demo.service.IDemoService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Luo Bao Ding
 * @since 2018/5/25
 */
@Component
public class DemoServiceImpl implements IDemoService {
    //*************mock data**************//
    private static List<ResultModel> resultModelList = new ArrayList<>();

    static {
        ResultModel model = new ResultModel();
        model.setId(1);
        model.setContent("This is first model");
        resultModelList.add(model);

        model = new ResultModel();
        model.setId(2);
        model.setContent("This is second model");
        resultModelList.add(model);
    }

    @Override
    public Mono<ResultViewModel> extraResult(ServerRequest request) {

        int id = Integer.parseInt(request.pathVariable("id"));
        ResultModel model = null;
        ResultViewModel viewModel;
        for (ResultModel resultModel : resultModelList) {
            if (resultModel.getId() == id) {
                model = resultModel;
                break;
            }
        }
        if (model != null) {
            viewModel = new ResultViewModel(200, "success", model);
        } else {
            viewModel = ResultViewModel.EMPTY_RESULT;
        }
        return Mono.just(viewModel);
    }

    @Override
    public Flux<ResultViewModel> flowAllResult(ServerRequest request) {
        List<ResultViewModel> resultViewModels = new ArrayList<>();
        for (ResultModel model : resultModelList) {
            resultViewModels.add(new ResultViewModel(200, "success", model));
        }

        return Flux.fromIterable(resultViewModels);

    }

    @Override
    public Mono<ResultViewModel> putItem(ServerRequest request) {
        Mono<ResultModel> modelMono = request.bodyToMono(ResultModel.class);

        final ResultViewModel[] views = new ResultViewModel[1];

        modelMono.doOnNext(resultModel -> {
            boolean isExist = false;
            for (ResultModel model : resultModelList) {
                if (model.getId() == resultModel.getId()) {
                    isExist = true;
                    break;
                }
            }
            if (!isExist) {
                resultModelList.add(resultModel);
                views[0] = new ResultViewModel(200, "success", resultModel);
            } else {
                views[0] = ResultViewModel.EMPTY_RESULT;
            }
        }).thenEmpty(Mono.empty());

        return Mono.just(views[0]);
    }

}
