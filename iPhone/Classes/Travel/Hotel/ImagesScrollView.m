//
//  ImagesScrollView.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 27/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ImagesScrollView.h"
#import "DownloadableUIImage.h"

@interface ImagesScrollView() <ImageDownloaderOperationDelegate>
@property (nonatomic) CGFloat width;
@property (nonatomic, strong) NSOperationQueue *imageDownloadQueue;
@end

@implementation ImagesScrollView

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        self.width = frame.size.width;
        [self setBackgroundColor:[UIColor blackColor]];
        // Initialization code
        UIButton *button = [[UIButton alloc] initWithFrame:CGRectMake(self.width -30 -15, 30, 30, 30)];
        button.backgroundColor = [UIColor clearColor];
        //        button.layer.cornerRadius = 4;
        //        button.layer.borderWidth = 1;
        //        button.layer.borderColor = [UIColor whiteColor].CGColor;
        //        [button setTitle:@"Done" forState:UIControlStateNormal];
//        [button setTitle:@"ⓧ" forState:UIControlStateNormal];
        [button setImage:[UIImage imageNamed:@"close"] forState:UIControlStateNormal];
        [button setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        button.titleLabel.textColor = [UIColor whiteColor];
        button.titleLabel.font = [UIFont boldSystemFontOfSize:20.0];
        [button addTarget:self action:@selector(closeView:) forControlEvents:UIControlEventTouchUpInside];
        self.closeButton = button;
        
    }
    return self;
}

- (NSOperationQueue *)imageDownloadQueue {
    if (!_imageDownloadQueue) {
        _imageDownloadQueue = [[NSOperationQueue alloc] init];
        _imageDownloadQueue.name = @"Full Screen Download Queue";
        // Let the OS manage
        _imageDownloadQueue.maxConcurrentOperationCount = 5;
    }
    return _imageDownloadQueue;
}

-(void)closeView:(id)sender
{
    [self removeFromSuperview];
}

-(void)setArrayOfDownloadableImages:(NSMutableArray *)array{
    _arrayOfDownloadableImages = array;
    pageControl = [[UIPageControl alloc] init];
    //pageControl.frame = CGRectMake((98/[[UIScreen mainScreen] bounds].size.width)*self.frame.size.width,(400/[[UIScreen mainScreen] bounds].size.height)*self.frame.size.height, 122, 36);
    pageControl.frame = CGRectMake(0,(430.0/480.0)*self.frame.size.height, self.frame.size.width, 36);
    pageControl.numberOfPages = self.arrayOfDownloadableImages.count;
    pageControl.currentPage = self.selectedIndex;
    
    scrollview = [[UIScrollView alloc]initWithFrame:self.frame];
    scrollview.contentSize = CGSizeMake(scrollview.frame.size.width * self.arrayOfDownloadableImages.count,scrollview.frame.size.height);
    [scrollview setDelegate:self];
    scrollview.showsVerticalScrollIndicator = NO;
    scrollview.showsHorizontalScrollIndicator = NO;
    scrollview.pagingEnabled = YES;
    for (int i =0; i < self.arrayOfDownloadableImages.count ; i++) {
        DownloadableUIImage *downloadableImage = self.arrayOfDownloadableImages[i];
        UIImageView *imageview = [[UIImageView alloc] init];
        imageview.image = downloadableImage.image;
        [imageview setContentMode:UIViewContentModeScaleAspectFit];
        imageview.frame = CGRectMake(0.0, 0.0,scrollview.frame.size.width , scrollview.frame.size.height);
        [imageview setTag:i+1];
        [self addSubview:imageview];
        if (i != self.selectedIndex) {
            imageview.alpha = 0;
        }
        else {
            [self checkIfImageIsAvailableForPage:self.selectedIndex withImageView:imageview];
        }
    }
    [scrollview scrollRectToVisible:CGRectMake(((pageControl.currentPage)%self.arrayOfDownloadableImages.count)*scrollview.frame.size.width, 0, scrollview.frame.size.width, scrollview.frame.size.height) animated:NO];
    [pageControl addTarget:self
                    action:@selector(pgCntlChanged:)forControlEvents:UIControlEventValueChanged];
    //[self performSelector:@selector(startAnimatingScrl) withObject:nil afterDelay:3.0];
    
    [self addSubview:scrollview];
    [self addSubview:pageControl];
    [self addSubview:self.closeButton];
    [self.closeButton sizeToFit];
    [pageControl sizeToFit];
}
- (void)startAnimatingScrl
{
    if (self.arrayOfDownloadableImages.count) {
        [scrollview scrollRectToVisible:CGRectMake(((pageControl.currentPage +1)%self.arrayOfDownloadableImages.count)*scrollview.frame.size.width, 0, scrollview.frame.size.width, scrollview.frame.size.height) animated:YES];
        [pageControl setCurrentPage:((pageControl.currentPage +1)%self.arrayOfDownloadableImages.count)];
        [self performSelector:@selector(startAnimatingScrl) withObject:nil  afterDelay:3.0];
    }
}
-(void) cancelScrollAnimation{
    didEndAnimate =YES;
    [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(startAnimatingScrl) object:nil];
}

- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView
{
    [self cancelScrollAnimation];
    previousTouchPoint = scrollView.contentOffset.x;
}

- (IBAction)pgCntlChanged:(UIPageControl *)sender {
    [scrollview scrollRectToVisible:CGRectMake(sender.currentPage*scrollview.frame.size.width, 0, scrollview.frame.size.width, scrollview.frame.size.height) animated:YES];
    [self cancelScrollAnimation];
}

- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView{
    NSInteger currentPage = scrollview.bounds.origin.x/scrollview.frame.size.width;
    [pageControl setCurrentPage:currentPage];
    [self checkIfImageIsAvailableForPage:currentPage withImageView:[self viewWithTag:currentPage+1]];
}

- (void)scrollViewDidScroll:(UIScrollView *)scrollView
{
    [scrollView setContentOffset: CGPointMake(scrollView.contentOffset.x, 0)];
    
    int page = floor((scrollView.contentOffset.x - self.width) / self.width) + 1;
    float OldMin = self.width*page;
    int Value = scrollView.contentOffset.x -OldMin;
    float alpha = (Value % (int)self.width) / self.width;
    
    
    if (previousTouchPoint > scrollView.contentOffset.x)
        page = page+2;
    else
        page = page+1;
    
    UIView *nextPage = [scrollView.superview viewWithTag:page+1];
    UIView *previousPage = [scrollView.superview viewWithTag:page-1];
    UIView *currentPage = [scrollView.superview viewWithTag:page];
    
    if(previousTouchPoint <= scrollView.contentOffset.x){
        if ([currentPage isKindOfClass:[UIImageView class]])
            currentPage.alpha = 1-alpha;
        if ([nextPage isKindOfClass:[UIImageView class]])
            nextPage.alpha = alpha;
        if ([previousPage isKindOfClass:[UIImageView class]])
            previousPage.alpha = 0;
    }else if(page != 0){
        if ([currentPage isKindOfClass:[UIImageView class]])
            currentPage.alpha = alpha;
        if ([nextPage isKindOfClass:[UIImageView class]])
            nextPage.alpha = 0;
        if ([previousPage isKindOfClass:[UIImageView class]])
            previousPage.alpha = 1-alpha;
    }
    if (!didEndAnimate && pageControl.currentPage == 0) {
        for (UIView * imageView in self.subviews){
            if (imageView.tag !=1 && [imageView isKindOfClass:[UIImageView class]])
                imageView.alpha = 0;
            else if([imageView isKindOfClass:[UIImageView class]])
                imageView.alpha = 1 ;
        }
    }
    
}

- (void)checkIfImageIsAvailableForPage:(int)pageNumber withImageView:(id)imageView
{
    if ([imageView isKindOfClass:[UIImageView class]]) {
        UIImageView *imgView = (UIImageView *)imageView;
        if (!imgView.image) {
            DownloadableUIImage *downloadableImage = (DownloadableUIImage *)self.arrayOfDownloadableImages[pageNumber];
            if (downloadableImage.hasImage) {
                imgView.image = downloadableImage.image;
            }
            else if (!downloadableImage.failed) {
                // Download image
                [self.hotelCellData downloadHotelImages:self.imageDownloadQueue indexPath:[NSIndexPath indexPathForItem:pageNumber inSection:0] downloadableUIImage:downloadableImage delegate:self];
            }
        }
    }
}

-(void)ImageDownloaderOperationDidFinish:(ImageDownloaderOperation *)downloader
{
    // get the big image to display
    if (downloader.downloadableImage.image != nil)
    {
        UIView *view = [self viewWithTag:downloader.indexPathInTableView.row+1];
        if ([view isKindOfClass:[UIImageView class]]) {
            UIImageView *imageView = (UIImageView *)view;
            imageView.image = downloader.downloadableImage.image;
        }
    }
}

-(void) dealloc
{
    [self cancelScrollAnimation];
    [self.imageDownloadQueue cancelAllOperations];
}

@end
