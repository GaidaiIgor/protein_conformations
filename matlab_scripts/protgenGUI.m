function varargout = protgenGUI(varargin)
% PROTGENGUI MATLAB code for protgenGUI.fig
%      PROTGENGUI, by itself, creates a new PROTGENGUI or raises the existing
%      singleton*.
%
%      H = PROTGENGUI returns the handle to a new PROTGENGUI or the handle to
%      the existing singleton*.
%
%      PROTGENGUI('CALLBACK',hObject,eventData,handles,...) calls the local
%      function named CALLBACK in PROTGENGUI.M with the given input arguments.
%
%      PROTGENGUI('Property','Value',...) creates a new PROTGENGUI or raises the
%      existing singleton*.  Starting from the left, property value pairs are
%      applied to the GUI before protgenGUI_OpeningFcn gets called.  An
%      unrecognized property name or invalid value makes property application
%      stop.  All inputs are passed to protgenGUI_OpeningFcn via varargin.
%
%      *See GUI Options on GUIDE's Tools menu.  Choose "GUI allows only one
%      instance to run (singleton)".
%
% See also: GUIDE, GUIDATA, GUIHANDLES

% Edit the above text to modify the response to help protgenGUI

% Last Modified by GUIDE v2.5 17-Nov-2014 05:17:49

% Begin initialization code - DO NOT EDIT
gui_Singleton = 1;
gui_State = struct('gui_Name',       mfilename, ...
                   'gui_Singleton',  gui_Singleton, ...
                   'gui_OpeningFcn', @protgenGUI_OpeningFcn, ...
                   'gui_OutputFcn',  @protgenGUI_OutputFcn, ...
                   'gui_LayoutFcn',  [] , ...
                   'gui_Callback',   []);
if nargin && ischar(varargin{1})
    gui_State.gui_Callback = str2func(varargin{1});
end

if nargout
    [varargout{1:nargout}] = gui_mainfcn(gui_State, varargin{:});
else
    gui_mainfcn(gui_State, varargin{:});
end
% End initialization code - DO NOT EDIT


% --- Executes just before protgenGUI is made visible.
function protgenGUI_OpeningFcn(hObject, eventdata, handles, varargin)
% This function has no output args, see OutputFcn.
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
% varargin   command line arguments to protgenGUI (see VARARGIN)
projectinit;

% Choose default command line output for protgenGUI
handles.output = hObject;

% Update handles structure
guidata(hObject, handles);

% UIWAIT makes protgenGUI wait for user response (see UIRESUME)
% uiwait(handles.figure1);


% --- Outputs from this function are returned to the command line.
function varargout = protgenGUI_OutputFcn(hObject, eventdata, handles) 
% varargout  cell array for returning output args (see VARARGOUT);
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Get default command line output from handles structure
varargout{1} = handles.output;


% --- Executes on button press in Start.
function Start_Callback(hObject, eventdata, handles)
% hObject    handle to Start (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

if get(handles.optimizeAllInitModels, 'Value')
    model = [];
else
    model = str2double(get(handles.initModelNumber,'String'));
end
getOptimizedTransformation(handles.pdbBegin, handles.pdbEnd,...
    str2double(get(handles.intermediateConformationsQty,'String')),...
    str2double(get(handles.anglesThreshold,'String')),...
    model, str2double(get(handles.optimizationAnglesCount,'String')),...
    @trmcostp, [2], str2double(get(handles.iterations,'String')),...
    [], handles.out);
guidata(hObject,handles);


% --- Executes during object creation, after setting all properties.
function beginpdb_CreateFcn(hObject, eventdata, handles)
% hObject    handle to beginpdb (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end


% --- Executes during object creation, after setting all properties.
function endpdb_CreateFcn(hObject, eventdata, handles)
% hObject    handle to endpdb (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end


% --- If Enable == 'on', executes on mouse press in 5 pixel border.
% --- Otherwise, executes on mouse press in 5 pixel border or over beginpdb.
function beginpdb_ButtonDownFcn(hObject, eventdata, handles)
% hObject    handle to beginpdb (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
[FileName, PathName] = uigetfile('*.pdb','Select pdb file with begin conformation');
handles.pdbBeginFileName = [PathName FileName];
set(handles.beginpdb, 'String', FileName);
handles.pdbBegin = pdbbackbone(pdbread(handles.pdbBeginFileName));
guidata(hObject,handles);


% --- If Enable == 'on', executes on mouse press in 5 pixel border.
% --- Otherwise, executes on mouse press in 5 pixel border or over endpdb.
function endpdb_ButtonDownFcn(hObject, eventdata, handles)
% hObject    handle to endpdb (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
[FileName, PathName] = uigetfile('*.pdb','Select pdb file with end conformation');
handles.pdbEndFileName = [PathName FileName];
set(handles.endpdb, 'String', FileName);
handles.pdbEnd = pdbbackbone(pdbread(handles.pdbEndFileName));
guidata(hObject,handles);


% --- Executes on button press in browsepdbbegin.
function browsepdbbegin_Callback(hObject, eventdata, handles)
% hObject    handle to browsepdbbegin (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
beginpdb_ButtonDownFcn(hObject, eventdata, handles);


% --- Executes on button press in browsepdbend.
function browsepdbend_Callback(hObject, eventdata, handles)
% hObject    handle to browsepdbend (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
endpdb_ButtonDownFcn(hObject, eventdata, handles);


% --- Executes on button press in selectOutFolder.
function selectOutFolder_Callback(hObject, eventdata, handles)
% hObject    handle to selectOutFolder (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
outfoldertxt_ButtonDownFcn(hObject, eventdata, handles);


% --- Executes during object creation, after setting all properties.
function optimizationAnglesCount_CreateFcn(hObject, eventdata, handles)
% hObject    handle to optimizationAnglesCount (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end



function anglesThreshold_Callback(hObject, eventdata, handles)
% hObject    handle to anglesThreshold (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of anglesThreshold as text
%        str2double(get(hObject,'String')) returns contents of anglesThreshold as a double
updateBidirectionalAnglesTableInfo(handles)

function updateBidirectionalAnglesTableInfo(handles)
anglesThreshold = str2double(get(handles.anglesThreshold, 'String'));
handles.initTrm = trmcreate(handles.pdbBegin, handles.pdbEnd, 0);
mostChangeableAngles = trmGetMostChangeableAngles(handles.initTrm, [], anglesThreshold);
l = int32(length(mostChangeableAngles));
t = cell(2^l,l);
v = dec2bin((1:2^l-1)',l);
for i = 1:2^l-1
    for j = 1:l
        if v(i,j)=='1'
            t(i+1,l-j+1) = {'*'};
        end
    end
end
set(handles.longArcTable,'data',t);
names=cell(length(mostChangeableAngles),1);
for i = 1:length(names)
    angleName = '';
    if handles.pdbBegin.Model.Atom(mostChangeableAngles(i)+1).AtomName == 'CA'
        angleName = 'psi';
    end
    if handles.pdbBegin.Model.Atom(mostChangeableAngles(i)+1).AtomName == 'N'
        angleName = 'phi';
    end
    names(i) = {[handles.pdbBegin.Model.Atom(mostChangeableAngles(i)+1).resName ...
    num2str(handles.pdbBegin.Model.Atom(mostChangeableAngles(i)+1).resSeq) ...
    '-' angleName]};   
end
set(handles.longArcTable,'columnname',names);


% --- Executes during object creation, after setting all properties.
function anglesThreshold_CreateFcn(hObject, eventdata, handles)
% hObject    handle to anglesThreshold (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end



function iterations_Callback(hObject, eventdata, handles)
% hObject    handle to iterations (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of iterations as text
%        str2double(get(hObject,'String')) returns contents of iterations as a double


% --- Executes during object creation, after setting all properties.
function iterations_CreateFcn(hObject, eventdata, handles)
% hObject    handle to iterations (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end


function initModelNumber_Callback(hObject, eventdata, handles)
% hObject    handle to initModelNumber (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of initModelNumber as text
%        str2double(get(hObject,'String')) returns contents of initModelNumber as a double


% --- Executes during object creation, after setting all properties.
function initModelNumber_CreateFcn(hObject, eventdata, handles)
% hObject    handle to initModelNumber (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end


% --- If Enable == 'on', executes on mouse press in 5 pixel border.
% --- Otherwise, executes on mouse press in 5 pixel border or over outfoldertxt.
function outfoldertxt_ButtonDownFcn(hObject, eventdata, handles)
% hObject    handle to outfoldertxt (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
handles.out = uigetdir('%HOMEPATH%','Select Output Destination Folder');
set(handles.outfoldertxt, 'String', handles.out);
guidata(hObject,handles);



function intermediateConformationsQty_Callback(hObject, eventdata, handles)
% hObject    handle to intermediateConformationsQty (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of intermediateConformationsQty as text
%        str2double(get(hObject,'String')) returns contents of intermediateConformationsQty as a double


% --- Executes during object creation, after setting all properties.
function intermediateConformationsQty_CreateFcn(hObject, eventdata, handles)
% hObject    handle to intermediateConformationsQty (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end


% --- If Enable == 'on', executes on mouse press in 5 pixel border.
% --- Otherwise, executes on mouse press in 5 pixel border or over optimizeAllInitModels.
function optimizeAllInitModels_Callback(hObject, eventdata, handles)
% hObject    handle to optimizeAllInitModels (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
if get(handles.optimizeAllInitModels, 'Value')
    set(handles.initModelNumber, 'enable', 'off');
else
    set(handles.initModelNumber, 'enable', 'on');
end


% --- Executes when selected cell(s) is changed in longArcTable.
function longArcTable_CellSelectionCallback(hObject, eventdata, handles)
% hObject    handle to longArcTable (see GCBO)
% eventdata  structure with the following fields (see UITABLE)
%	Indices: row and column indices of the cell(s) currently selecteds
% handles    structure with handles and user data (see GUIDATA)
updateBidirectionalAnglesTableInfo(handles)


% --- Executes on key press with focus on longArcTable and none of its controls.
function longArcTable_KeyPressFcn(hObject, eventdata, handles)
% hObject    handle to longArcTable (see GCBO)
% eventdata  structure with the following fields (see UITABLE)
%	Key: name of the key that was pressed, in lower case
%	Character: character interpretation of the key(s) that was pressed
%	Modifier: name(s) of the modifier key(s) (i.e., control, shift) pressed
% handles    structure with handles and user data (see GUIDATA)
updateBidirectionalAnglesTableInfo(handles)


% --- Executes on button press in updateTable.
function updateTable_Callback(hObject, eventdata, handles)
% hObject    handle to updateTable (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
updateBidirectionalAnglesTableInfo(handles)
