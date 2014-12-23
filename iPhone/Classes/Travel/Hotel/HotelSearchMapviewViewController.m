//
//  HotelSearchMapviewViewController.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/16/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelSearchMapviewViewController.h"
#import "CTEHotelCellData.h"
#import "HotelSearchTableViewCell.h"
#import "HotelAnnotation.h"
#import "HotelRoomsListTableViewController.h"

@interface HotelSearchMapviewViewController ()

@property NSInteger selectedIndex;
@property (nonatomic, strong) UIToolbar *toolBarForTableView ;

@end

@implementation HotelSearchMapviewViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self.navigationController setNavigationBarHidden:NO];
    if(!self.isSingleMapView)
        self.title = [@"Hotels" localize];

    // Turn on the user location
    self.mapView.delegate = self;
    self.mapView.showsUserLocation = YES;
    
    [self.tableView registerNib:[UINib nibWithNibName:@"HotelSearchTableViewCell" bundle:nil] forCellReuseIdentifier:@"HotelSearchTableViewCell"];
    
    UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithTitle:[@"Close" localize]
                                                                   style:UIBarButtonItemStyleDone
                                                                  target:self
                                                                  action:@selector(dismissMapView)];
    [self.navigationItem setLeftBarButtonItem:doneButton];

    // Show the table view in the toolbar so that it has the blur affect
    self.selectedIndex = 0;
    _toolBarForTableView = [[UIToolbar alloc] initWithFrame:self.tableView.frame];
    _toolBarForTableView.barStyle = UIBarStyleDefault;
    [self.tableView.superview insertSubview:_toolBarForTableView belowSubview:self.tableView];
    
}


-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.navigationController setToolbarHidden:YES];
   // Add a custom toolbar view so we get the motion blur effect for the view
    [self.tableView setHidden:NO];
    self.tableView.backgroundColor = [UIColor clearColor];
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self addAnnotations];
}

-(void)viewDidLayoutSubviews{
    [super viewDidLayoutSubviews];
    [self adjustTableHeight];
}

-(void)adjustTableHeight
{
    CGRect newFrame = self.tableView.frame;
    CTEHotelCellData *cellData =  (CTEHotelCellData *)[self.hotelList objectAtIndex:self.selectedIndex];
    // Make sure that the tableview y is also adjusted.
    
    newFrame.origin.y = self.view.layer.frame.size.height - cellData.cellHeight;
    newFrame.size.height = cellData.cellHeight;
    
    [UIView animateWithDuration:0.25 animations:^{
        _toolBarForTableView.frame = newFrame;
        [self.tableView setFrame:newFrame];
        
        // if you have other controls that should be resized/moved to accommodate
        // the resized tableview, do that here, too
    }];
}

#pragma mark MKMapViewDelegate methods


-(MKAnnotationView *)mapView:(MKMapView *)thismapView viewForAnnotation:(id <MKAnnotation>)annotation
{
    if (annotation == thismapView.userLocation)
    {
        // Do not add a custom pin for the user location, allow default to appear
        return nil;
    }
    else
    {
        static NSString *HotelAnnotationIdentifier = @"HotelAnnotationIdentifier";
        
        __autoreleasing MKPinAnnotationView	*annotationView = (MKPinAnnotationView*) [thismapView dequeueReusableAnnotationViewWithIdentifier:HotelAnnotationIdentifier];
        if (annotationView == nil)
            annotationView = [[MKPinAnnotationView alloc] initWithAnnotation:annotation reuseIdentifier:HotelAnnotationIdentifier];
        
        // Customize the pin
        HotelAnnotation *hotelAnnotation = (HotelAnnotation *)annotation;

        //We always show the first one by default. show the selected hotel index pin in different color
        if (hotelAnnotation.hotelIndex == self.selectedIndex) {
             annotationView.image = [UIImage imageNamed:@"icon_map_pin_selected"];
        }
        else
            annotationView.image = [UIImage imageNamed:@"icon_map_pin"];
        
        annotationView.canShowCallout = NO;
        return annotationView;
    }
}

- (void)mapView:(MKMapView *)mapView didSelectAnnotationView:(MKAnnotationView *)view
{
    // We don't want selecting the current location to clear a hotel selection
    if (view.annotation != mapView.userLocation)
    {
        [mapView deselectAnnotation:view.annotation animated:YES];
        
        HotelAnnotation *annotation = (HotelAnnotation *)view.annotation;
        
        // Set the old selected pin back to its old view
        // Hotels are added in the same order as in the hotellist so the index of previous pin 
        if (self.selectedIndex != -1) {
            
            for (HotelAnnotation *ann in mapView.annotations)
            {
                // Only affect hotel location annotations - leave user location alone
                if ((MKUserLocation*)ann != mapView.userLocation)
                {
                    if (ann.hotelIndex == self.selectedIndex) {
                        MKPinAnnotationView *oldAnnotationView = (MKPinAnnotationView*)[mapView viewForAnnotation:ann];
                        oldAnnotationView.image = [UIImage imageNamed:@"icon_map_pin"];
                    }
                }
            }

        }

        self.selectedIndex = annotation.hotelIndex;
        // Set the selected pin color to different color.
        view.image = [UIImage imageNamed:@"icon_map_pin_selected"];
        
        DLog(@" selected view index: %d ", self.selectedIndex);
        [self adjustTableHeight];
        [self.tableView reloadData];
    }
}

#pragma mark - animate the pin drop

- (void)mapView:(MKMapView *)mapView didAddAnnotationViews:(NSArray *)views {
    
    // Set the map zoom based on the list of annotations
    //This Logic copied from stack overflow. i tweaked the last two lines to adjust to fit in our view.
    
    MKAnnotationView *aV;
    for (aV in views) {
        CGRect endFrame = aV.frame;
        
        aV.frame = CGRectMake(aV.frame.origin.x, aV.frame.origin.y - 230.0, aV.frame.size.width, aV.frame.size.height);
        
        [UIView beginAnimations:nil context:NULL];
        [UIView setAnimationDuration:0.45];
        [UIView setAnimationCurve:UIViewAnimationCurveEaseInOut];
        [aV setFrame:endFrame];
        [UIView commitAnimations];
    }
}


#pragma mark - UITableviewSourceDelegate

-(NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Dont show anything until user selects a hotel
    if (self.selectedIndex == -1) {
        return 0;
    }
      // Always 1 row
    return 1;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Dont show anything until user selects a hotel
    if (self.selectedIndex < 0) {
        return 0;
    }

    CTEHotelCellData *cellData =  (CTEHotelCellData *)[self.hotelList objectAtIndex:self.selectedIndex];
    return cellData.cellHeight;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	// if selected index is nil then dont show anything. 
    if (self.selectedIndex < 0) {
        return 0;
    }
    // Always 1 row
    return 1;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{

    CTEHotelCellData *cellData =  (CTEHotelCellData *)[self.hotelList objectAtIndex:self.selectedIndex];
    HotelSearchTableViewCell *hotelListCell = [self.tableView dequeueReusableCellWithIdentifier:cellData.cellIdentifier forIndexPath:indexPath];
 
    if (hotelListCell == nil)
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"HotelSearchTableViewCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[HotelSearchTableViewCell class]])
                hotelListCell = (HotelSearchTableViewCell *)oneObject;
    }
    if (self.isSingleMapView) {
        [self.tableView setHidden:YES];
        [self.toolBarForTableView setHidden:YES];
    }else{
        [self.tableView setHidden:NO];
        [self.toolBarForTableView setHidden:NO];
    }

    [hotelListCell setCellData:cellData indexPath:indexPath];

    // Set the cell to be transparent so we can see the blur affect.
    hotelListCell.backgroundColor = [UIColor clearColor];
    hotelListCell.contentView.backgroundColor = [UIColor clearColor];
    
    return hotelListCell;

}

#pragma mark - UITableViewDelegate
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (!self.isSingleMapView)
    {
        [self performSegueWithIdentifier:@"hotelRoomsListFromMap" sender:self];
    }
    
    [self.tableView deselectRowAtIndexPath:indexPath animated:NO];
}

#pragma mark - map methods

/*! Add hotel annotations to the mapview
*/
-(void)addAnnotations
{
    for (int index = 0; index < [self.hotelList count]; index++) {
        CTEHotelCellData *cteHotelCellData = [self.hotelList objectAtIndex:index];
       // TODO: Clarify map plotting requirements
      // Sent map annotation
      [self.mapView addAnnotation:[cteHotelCellData getAnnotationWithIndex:index]];
  }
    
    if([self.mapView.annotations count] == 0)
        return;
    
    CLLocationCoordinate2D topLeftCoord;
    topLeftCoord.latitude = -90;
    topLeftCoord.longitude = 180;
    
    CLLocationCoordinate2D bottomRightCoord;
    bottomRightCoord.latitude = 90;
    bottomRightCoord.longitude = -180;
    
    for(id <MKAnnotation> annotation in self.mapView.annotations)
    {
        if ((MKUserLocation*)annotation != self.mapView.userLocation)
        {
            topLeftCoord.longitude = fmin(topLeftCoord.longitude, annotation.coordinate.longitude);
            topLeftCoord.latitude = fmax(topLeftCoord.latitude, annotation.coordinate.latitude);
            
            bottomRightCoord.longitude = fmax(bottomRightCoord.longitude, annotation.coordinate.longitude);
            bottomRightCoord.latitude = fmin(bottomRightCoord.latitude, annotation.coordinate.latitude);
        }
        else{
            // If we are showing the user location, then ensure that the region contains this as well
            if (self.mapView.showsUserLocation)
            {
                CLLocation *currentLocation = [[CLLocation alloc] initWithLatitude:annotation.coordinate.latitude longitude:annotation.coordinate.longitude];
                CLLocation *topLeftLocation = [[CLLocation alloc] initWithLatitude:topLeftCoord.latitude longitude:topLeftCoord.longitude];
                CLLocation *bottomRightLocation = [[CLLocation alloc] initWithLatitude:bottomRightCoord.latitude longitude:bottomRightCoord.longitude];

                // We only want to include the current location in the region if we are within a reasonable
                // distance of the other annotations, using 200 metres as the guide
                CLLocationDistance distance1 = [currentLocation distanceFromLocation: topLeftLocation];
                CLLocationDistance distance2 = [currentLocation distanceFromLocation: bottomRightLocation];
                
                if (distance1 < 200 && distance2 < 200)
                {
                    topLeftCoord.longitude = fmin(topLeftCoord.longitude, currentLocation.coordinate.longitude);
                    topLeftCoord.latitude = fmax(topLeftCoord.latitude, currentLocation.coordinate.latitude);
                    
                    bottomRightCoord.longitude = fmax(bottomRightCoord.longitude, currentLocation.coordinate.longitude);
                    bottomRightCoord.latitude = fmin(bottomRightCoord.latitude, currentLocation.coordinate.latitude);
                }
            }
        }
    }

    MKCoordinateRegion region;
    region.center.latitude = topLeftCoord.latitude - (topLeftCoord.latitude - bottomRightCoord.latitude) * 0.5;
    region.center.longitude = topLeftCoord.longitude + (bottomRightCoord.longitude - topLeftCoord.longitude) * 0.5;
    region.span.latitudeDelta = fabs(topLeftCoord.latitude - bottomRightCoord.latitude) * 1.8; // Add a little extra space on the sides
    region.span.longitudeDelta = fabs(bottomRightCoord.longitude - topLeftCoord.longitude) * 1.8; // Add a little extra space on the sides
    
    region = [self.mapView regionThatFits:region];
    [self.mapView setRegion:region animated:YES];
 
}


#pragma mark - Navigation

-(void)dismissMapView
{
    [self.navigationController dismissViewControllerAnimated:YES completion:nil];
}

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if(self.isSingleMapView)    // if showing only single hotel on map then dont go anywhere from here. 
    {
        return;
    }

    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    if ([segue.identifier isEqualToString:@"hotelRoomsListFromMap"])
    {
        CTEHotelCellData *cellData =  (CTEHotelCellData *)[self.hotelList objectAtIndex:self.selectedIndex];
        HotelRoomsListTableViewController *roomsListViewController = segue.destinationViewController;
        roomsListViewController.hotelCellData = cellData;
    }
}

#pragma mark - memory managment
- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
    
}

-(void)dealloc
{
    self.mapView = nil;
    self.hotelList = nil;
}

@end
